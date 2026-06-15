package snownee.jade.overlay;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.api.AccessorClientHandler;
import snownee.jade.api.EmptyAccessor;
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
	public static final String REMOVE_ELEMENTS = "$jade:remove";
	public static final MapCodec<List<Identifier>> REMOVE_ELEMENTS_CODEC = Identifier.CODEC.listOf().fieldOf(REMOVE_ELEMENTS);

	private String lastNarration = "";
	private long lastNarrationTime = 0;
	public ProgressTracker progressTracker = new ProgressTracker();
	public @Nullable BoxElementImpl rootElement;
	public @Nullable State state;

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

	public void clearState() {
		lastNarration = "";
		state = null;
		rootElement = null;
		progressTracker.clear();
	}

	@SuppressWarnings("deprecation")
	public void tickClient() {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		if (level == null) {
			OverlayRenderer.clearLingerTooltip();
			clearState();
			return;
		}

		progressTracker.tick();

		General config = IWailaConfig.get().general();
		if (!config.shouldDisplayTooltip()) {
			clearState();
			return;
		}

		if (JadeUI.isPinned()) {
			return;
		}

		if (ClientProxy.shouldHideWithGui(mc, mc.gui.screen())) {
			return;
		}

		Entity entity = mc.getCameraEntity();
		if (entity == null) {
			clearState();
			return;
		}

		RayTracing.INSTANCE.fire();
		HitResult target = RayTracing.INSTANCE.getTarget();
		if (target == null) {
			clearState();
			return;
		}

		Accessor<?> accessor;
		boolean useRayTraceCallback = true;
		outer:
		if (target instanceof BlockHitResult blockTarget && blockTarget.getType() != HitResult.Type.MISS) {
			BlockState state = level.getBlockState(blockTarget.getBlockPos());
			if (state.isAir()) {
				accessor = createEmpty(target);
				break outer;
			}
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
		} else if (mc.gui.screen() instanceof PreviewOptionsScreen) {
			useRayTraceCallback = false;
			/* off */
			accessor = WailaClientRegistration.instance().blockAccessor()
					.blockState(Blocks.GRASS_BLOCK.defaultBlockState())
					.hit(new BlockHitResult(entity.position(), Direction.UP, entity.blockPosition(), false))
					.build();
			/* on */
		} else {
			accessor = createEmpty(target);
		}

		if (useRayTraceCallback) {
			Accessor<?> originalAccessor = accessor;
			EmptyAccessor originalEmpty = originalAccessor instanceof EmptyAccessor emptyAccessor ? emptyAccessor : null;
			for (JadeRayTraceCallback callback : WailaClientRegistration.instance().rayTraceCallback.callbacks()) {
				accessor = callback.onRayTrace(target, accessor, originalAccessor);
				if (accessor == null) {
					if (originalEmpty == null) {
						originalEmpty = createEmpty(originalAccessor.getHitResult());
					}
					accessor = originalEmpty;
				}
			}
		}

		if (!accessor.verifyData(accessor.getServerData())) {
			accessor.setServerData(null);
		}

		ObjectDataCenter.set(accessor);
		var handler = WailaClientRegistration.instance().getAccessorHandler(accessor.getAccessorType());

		if (!handler.shouldDisplay(accessor)) {
			clearState();
			return;
		}

		state = State.create(state, accessor, handler, state == null ? null : state.data);
		if (accessor.isServerConnected()) {
			CompoundTag data = accessor.getServerData();
			accessor.setServerData(null);
			List<IServerDataProvider<Accessor<?>>> providers = handler.shouldRequestData(accessor);
			if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
				ObjectDataCenter.resetTimer();
				if (!providers.isEmpty()) {
					handler.requestData(accessor, providers);
				}
			}
			if (!providers.isEmpty() && getData() == null) {
				return;
			}
			accessor.setServerData(data);
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
		tooltip.setIcon(state.getIcon());

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

		if (accessor.isServersideContent()) {
			CustomData data = accessor.getServersideRep().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
			if (data.tag.contains(REMOVE_ELEMENTS)) {
				List<Identifier> list = data.tag.read(REMOVE_ELEMENTS_CODEC).orElse(List.of());
				for (Identifier tag : list) {
					tooltip.remove(tag);
				}
			}
		}

		tooltip.setIcon(themes.theme().modifyIcon(tooltip.getIcon()));
		BoxElementImpl newElement = new BoxElementImpl(tooltip, themes.theme().tooltipStyle);
		newElement.tag(JadeIds.ROOT);
		for (JadeTooltipCollectedCallback callback : WailaClientRegistration.instance().tooltipCollectedCallback.callbacks()) {
			callback.onTooltipCollected(newElement, accessor);
		}
		if (newElement.getTooltip().isDirty) {
			newElement.updateSize();
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

	private static EmptyAccessor createEmpty(HitResult hit) {
		BlockHitResult miss;
		if (hit instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.MISS) {
			miss = blockHitResult;
		} else {
			Vec3 vec = hit.getLocation();
			miss = BlockHitResult.miss(
					vec,
					Direction.getApproximateNearest(vec.x, vec.y, vec.z),
					BlockPos.containing(vec));
		}
		return WailaClientRegistration.instance().emptyAccessor().hit(miss).build();
	}

	public void setData(CompoundTag tag) {
		if (state == null) {
			return;
		}
		state = state.withData(tag);
	}

	public @Nullable CompoundTag getData() {
		return state == null ? null : state.data;
	}

	public record State(Accessor<?> accessor, AccessorClientHandler<Accessor<?>> handler, @Nullable CompoundTag data) {
		public static State create(
				@Nullable State prev,
				Accessor<?> accessor,
				AccessorClientHandler<Accessor<?>> handler,
				@Nullable CompoundTag data) {
			return new State(accessor, handler, data != null && accessor.verifyData(data) ? data : null);
		}

		@Nullable
		public Element getIcon() {
			if (accessor == null || handler == null) {
				return null;
			}
			Element icon = handler.getIcon(accessor);
			if (JadeUI.isEmptyElement(icon)) {
				return null;
			}
			return icon;
		}

		public State withData(CompoundTag data) {
			if (!verifyData(data)) {
				return this;
			}
			return new State(accessor, handler, data);
		}

		public boolean verifyData(CompoundTag data) {
			if (data == null) {
				return true;
			}
			return accessor.verifyData(data);
		}
	}
}
