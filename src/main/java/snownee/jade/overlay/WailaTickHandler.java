package snownee.jade.overlay;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.base.Preconditions;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.api.callback.JadeBeforeTooltipCollectCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.General;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.ui.BoxElementImpl;
import snownee.jade.track.ProgressTracker;
import snownee.jade.util.ClientProxy;

public class WailaTickHandler {
	private String lastNarration = "";
	private long lastNarrationTime = 0;
	public BoxElementImpl rootElement;
	public ProgressTracker progressTracker = new ProgressTracker();

	public void narrate(Element element, boolean dedupe) {
		if (System.currentTimeMillis() - lastNarrationTime < 500) {
			return;
		}
		Component component = element.cachedNarration();
		if (component == null) {
			return;
		}
		narrate(StringUtil.stripColor(component.getString()), dedupe);
		lastNarrationTime = System.currentTimeMillis();
	}

	public void narrate(String message, boolean dedupe) {
		if (message.isEmpty()) {
			return;
		}
		if (dedupe && message.equals(lastNarration)) {
			return;
		}
		CompletableFuture.runAsync(() -> {
			GameNarrator narrator = Minecraft.getInstance().getNarrator();
			narrator.logNarratedMessage(message);
			if (IWailaConfig.get().general().isDebug()) {
				Jade.LOGGER.info("Narrating: {}", message);
			}
			if (narrator.isActive()) {
				narrator.clear();
				narrator.narrateMessage(message, true);
			}
		});
		lastNarration = message;
	}

	public void clearLastNarration() {
		lastNarration = "";
	}

	public void tickClient() {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		if (level == null) {
			rootElement = null;
			progressTracker.clear();
			OverlayRenderer.clearState();
			return;
		}

		progressTracker.tick();

		General config = IWailaConfig.get().general();
		if (!config.shouldDisplayTooltip()) {
			rootElement = null;
			return;
		}

		if (JadeUI.isPinned()) {
			return;
		}

		if (!ClientProxy.shouldShowWithGui(mc, mc.screen)) {
			return;
		}

		Entity entity = mc.getCameraEntity();
		if (entity == null) {
			rootElement = null;
			return;
		}

		RayTracing.INSTANCE.fire();
		HitResult target = RayTracing.INSTANCE.getTarget();
		if (target == null) {
			rootElement = null;
			return;
		}

		Accessor<?> accessor = null;
		if (target instanceof BlockHitResult blockTarget && blockTarget.getType() != HitResult.Type.MISS) {
			BlockState state = RayTracing.wrapBlock(level, blockTarget, CollisionContext.of(entity));
			BlockEntity tileEntity = level.getBlockEntity(blockTarget.getBlockPos());
			/* off */
			accessor = WailaClientRegistration.instance().blockAccessor()
					.blockState(state)
					.blockEntity(tileEntity)
					.hit(blockTarget)
					.requireVerification()
					.build();
			/* on */
		} else if (target instanceof EntityHitResult entityTarget) {
			/* off */
			accessor = WailaClientRegistration.instance().entityAccessor()
					.hit(entityTarget)
					.entity(entityTarget.getEntity())
					.requireVerification()
					.build();
			/* on */
		} else if (mc.screen instanceof PreviewOptionsScreen) {
			/* off */
			accessor = WailaClientRegistration.instance().blockAccessor()
					.blockState(Blocks.GRASS_BLOCK.defaultBlockState())
					.hit(new BlockHitResult(entity.position(), Direction.UP, entity.blockPosition(), false))
					.build();
			/* on */
		}

		Accessor<?> originalAccessor = accessor;
		for (JadeRayTraceCallback callback : WailaClientRegistration.instance().rayTraceCallback.callbacks()) {
			accessor = callback.onRayTrace(target, accessor, originalAccessor);
		}
		ObjectDataCenter.set(accessor);
		if (accessor == null || accessor.getHitResult() == null) {
			rootElement = null;
			return;
		}

		var handler = WailaClientRegistration.instance().getAccessorHandler(accessor.getAccessorType());
		if (!handler.shouldDisplay(accessor)) {
			rootElement = null;
			return;
		}
		if (accessor.isServerConnected()) {
			if (!accessor.verifyData(accessor.getServerData())) {
				accessor.getServerData().keySet().clear();
			}
			List<IServerDataProvider<Accessor<?>>> providers = handler.shouldRequestData(accessor);
			if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
				ObjectDataCenter.resetTimer();
				if (!providers.isEmpty()) {
					handler.requestData(accessor, providers);
				}
			}
			if (!providers.isEmpty() && ObjectDataCenter.getServerData() == null) {
				return;
			}
		}

		Theme theme = IWailaConfig.get().overlay().getTheme();
		MutableObject<Theme> holder = new MutableObject<>(theme);
		Preconditions.checkNotNull(theme, "Theme cannot be null");
		Accessor<?> accessor0 = accessor;
		for (JadeBeforeTooltipCollectCallback callback : WailaClientRegistration.instance().beforeTooltipCollectCallback.callbacks()) {
			if (!callback.beforeCollecting(holder, accessor0)) {
				return;
			}
		}
		Preconditions.checkNotNull(holder.getValue(), "Theme cannot be null");
		IThemeHelper themes = IThemeHelper.get();
		if (theme != holder.getValue()) {
			theme = holder.getValue();
			themes.setThemeOverride(theme);
		}

		Tooltip tooltip = new Tooltip();
		Element icon = ObjectDataCenter.getIcon();
		tooltip.setIcon(icon);

		if (config.getDisplayMode() == DisplayMode.LITE && !ClientProxy.isShowDetailsPressed()) {
			Tooltip dummyTooltip = new Tooltip();
			handler.gatherComponents(
					accessor, $ -> {
						if (Math.abs(WailaCommonRegistration.instance().priorities.byValue($)) > 5000) {
							return tooltip;
						} else {
							return dummyTooltip;
						}
					});
			if (!dummyTooltip.isEmpty()) {
				tooltip.sneakyDetails = true;
			}
		} else {
			handler.gatherComponents(accessor, $ -> tooltip);
		}

		tooltip.setIcon(themes.theme().modifyIcon(tooltip.getIcon()));
		BoxElementImpl newElement = new BoxElementImpl(tooltip, themes.theme().tooltipStyle);
		newElement.tag(JadeIds.ROOT);
		for (JadeTooltipCollectedCallback callback : WailaClientRegistration.instance().tooltipCollectedCallback.callbacks()) {
			callback.onTooltipCollected(newElement, accessor);
		}
		if (rootElement == null || rootElement.layout.getX() != newElement.layout.getX() ||
				rootElement.layout.getY() != newElement.layout.getY() ||
				rootElement.layout.getWidth() != newElement.layout.getWidth() ||
				rootElement.layout.getHeight() != newElement.layout.getHeight()) {
			OverlayRenderer.animation.startRect.copy(OverlayRenderer.animation.rect);
			OverlayRenderer.animation.startTime = System.currentTimeMillis();
		}
		rootElement = newElement;
		themes.setThemeOverride(null);
	}
}
