package snownee.jade.overlay;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.text2speech.Narrator;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
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
import snownee.jade.api.Accessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.impl.ui.BoxElement;
import snownee.jade.track.ProgressTracker;
import snownee.jade.util.ClientProxy;

public class WailaTickHandler {

	private static final Supplier<Narrator> NARRATOR = Suppliers.memoize(Narrator::getNarrator);
	private static WailaTickHandler INSTANCE = new WailaTickHandler();
	private static String lastNarration = "";
	private static long lastNarrationTime = 0;
	public BoxElement rootElement;
	public ProgressTracker progressTracker = new ProgressTracker();

	public static WailaTickHandler instance() {
		if (INSTANCE == null) {
			INSTANCE = new WailaTickHandler();
		}
		return INSTANCE;
	}

	public static void narrate(ITooltip tooltip, boolean dedupe) {
		if (!NARRATOR.get().active() || tooltip.isEmpty()) {
			return;
		}
		if (System.currentTimeMillis() - lastNarrationTime < 500) {
			return;
		}
		String narration = tooltip.getMessage();
		if (dedupe && narration.equals(lastNarration)) {
			return;
		}
		CompletableFuture.runAsync(() -> {
			Narrator narrator = NARRATOR.get();
			narrator.clear();
			narrator.say(StringUtil.stripColor(narration), false);
		});
		lastNarration = narration;
		lastNarrationTime = System.currentTimeMillis();
	}

	public static void clearLastNarration() {
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

		IWailaConfig.IConfigGeneral config = IWailaConfig.get().getGeneral();
		if (!config.shouldDisplayTooltip()) {
			rootElement = null;
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

		Tooltip tooltip = new Tooltip();

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
				accessor.getServerData().getAllKeys().clear();
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

		Theme theme = IWailaConfig.get().getOverlay().getTheme();
		ThemeHelper.theme.setValue(theme);
		Preconditions.checkNotNull(theme, "Theme cannot be null");
		Accessor<?> accessor0 = accessor;
		WailaClientRegistration.instance().beforeTooltipCollectCallback.call(callback -> {
			callback.beforeCollecting(ThemeHelper.theme, accessor0);
		});
		Preconditions.checkNotNull(ThemeHelper.theme.getValue(), "Theme cannot be null");

		if (config.getDisplayMode() == DisplayMode.LITE && !ClientProxy.isShowDetailsPressed()) {
			Tooltip dummyTooltip = new Tooltip();
			handler.gatherComponents(accessor, $ -> {
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

		BoxElement newElement = new BoxElement(tooltip, IThemeHelper.get().theme().tooltipStyle);
		newElement.tag(JadeIds.ROOT);
		newElement.setThemeIcon(RayTracing.INSTANCE.getIcon(), IThemeHelper.get().theme());
		for (JadeTooltipCollectedCallback callback : WailaClientRegistration.instance().tooltipCollectedCallback.callbacks()) {
			callback.onTooltipCollected(newElement, accessor);
		}
		rootElement = newElement;
		ThemeHelper.theme.setValue(theme);
	}
}
