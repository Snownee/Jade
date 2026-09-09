package snownee.jade.impl.ui;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.JadeClient;
import snownee.jade.JadeInternals;
import snownee.jade.api.Accessor;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.Overlay;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxProgress;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IBoxProgressProvider;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.ui.TooltipAnimation;
import snownee.jade.gui.JadeLinearLayout;
import snownee.jade.gui.LayoutWithPadding;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.gui.ResizeableLayout;
import snownee.jade.impl.Tooltip;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.track.BoxProgressFadeTrackInfo;
import snownee.jade.track.ProgressTracker;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.ToFloatFunction;
import snownee.jade.util.WailaExceptionHandler;

public class BoxElementImpl extends BoxElement implements ContainerEventHandler {
	public LayoutWithPadding layout;
	private final Tooltip tooltip;
	private final BoxStyle style;
	private final List<Renderable> renderables;
	private @Nullable List<AbstractWidget> widgets;
	private @Nullable List<GuiEventListener> eventListeners;
	private @Nullable Element icon;
	private static final int STATIC_PROGRESS_PRIORITY = Integer.MAX_VALUE;
	private final List<PrioritizedProvider> providers = new ArrayList<>();
	private @Nullable BoxProgress pendingProgress;
	private @Nullable BoxProgress fadeProgress;
	private float fadeAlpha;
	private @Nullable BoxProgressFadeTrackInfo fadeTrack;

	public BoxElementImpl(Tooltip tooltip, BoxStyle style) {
		this.tooltip = Objects.requireNonNull(tooltip);
		this.style = Objects.requireNonNull(style);
		this.icon = tooltip.getIcon();
		renderables = Lists.newArrayListWithExpectedSize(tooltip.size() + 1);
		updateSize();
	}

	@Override
	public void updateSize() {
		JadeLinearLayout linearLayout = JadeLinearLayout.vertical().alignItems(JadeLinearLayout.Align.STRETCH);
		for (Tooltip.Line line : tooltip.lines) {
			JadeLinearLayout lineLayout = JadeLinearLayout.horizontal();
			for (LayoutElement element : line.elements()) {
				if (element instanceof ResizeableElement resizeableElement) {
					resizeableElement.updateSize();
				}
				lineLayout.addChild(
						element, lineLayout.newChildLayoutSettings(element), container -> {
							if (container.child instanceof Element element0 && element0.getAlignSelf() != null) {
								container.alignSelf = element0.getAlignSelf();
							}
							if (container.child instanceof ResizeableLayout resizeableLayout) {
								container.flexGrow = resizeableLayout.getFlexGrow();
							}
						});
			}
			LayoutSettings lineSettings = linearLayout.newChildLayoutSettings(lineLayout);
			if (line.settings != null) {
				lineSettings = line.settings.apply(lineSettings);
			}
			linearLayout.addChild(
					lineLayout, lineSettings, container -> {
						container.headMargin = line.marginTop;
						container.tailMargin = line.marginBottom;
					});
		}
		tooltip.isDirty = false;

		if (icon != null) {
			JadeLinearLayout iconLayout = JadeLinearLayout.horizontal().alignItems(JadeLinearLayout.Align.START).spacing(3);
			IWailaConfig.IconMode iconMode = IWailaConfig.get().overlay().getIconMode();
			if (iconMode == IWailaConfig.IconMode.CENTERED) {
				iconLayout.alignItems(JadeLinearLayout.Align.CENTER);
			} else if (iconMode == IWailaConfig.IconMode.TOP && icon.getHeight() > linearLayout.getHeight()) {
				iconLayout.alignItems(JadeLinearLayout.Align.CENTER);
			}
			iconLayout.addChild(icon);
			iconLayout.addChild(linearLayout);
			linearLayout = iconLayout;
		}

		layout = new LayoutWithPadding(
				linearLayout,
				style.padding(ScreenDirection.LEFT),
				style.padding(ScreenDirection.UP),
				style.padding(ScreenDirection.RIGHT),
				style.padding(ScreenDirection.DOWN));
		layout.arrangeElements();
		width = layout.getWidth();
		height = layout.getHeight();

		renderables.clear();
		JadeUI.visitChildrenRecursive(
				layout, element -> {
					if (element instanceof Renderable renderable) {
						renderables.add(renderable);
					}
				});
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		layout.setX(x);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		layout.setY(y);
	}

	private static void chase(TooltipAnimation animation, ToFloatFunction<Rect2f> getter, FloatConsumer setter, float progress) {
		if (IWailaConfig.get().overlay().getAnimation()) {
			float source = getter.applyAsFloat(animation.rect);
			float target = getter.applyAsFloat(animation.expectedRect);
			float diff = target - source;
			if (diff == 0) {
				return;
			}
			if (progress >= 1) {
				animation.startTime = -1;
				setter.accept(target);
				return;
			}
			float startValue = getter.applyAsFloat(animation.startRect);
			float deltaValue = target - startValue;
			setter.accept(startValue + progress * deltaValue);
		} else {
			setter.accept(getter.applyAsFloat(animation.expectedRect));
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (tooltip.isEmpty()) {
			return;
		}

		// render background
		float alpha = IDisplayHelper.get().backgroundOpacity();
		boolean root = JadeIds.ROOT.equals(getTag());
		if (root) {
			alpha *= IWailaConfig.get().overlay().getAlpha();
		}
		if (alpha > 0) {
			style.render(graphics, this, getX(), getY(), getWidth(), getHeight(), alpha);
		}

		graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
		for (Renderable renderable : renderables) {
			try {
				renderable.extractRenderState(graphics, mouseX, mouseY, partialTicks);
			} catch (Exception e) {
				WailaExceptionHandler.handleErr(e, null, null);
				IDisplayHelper.get().drawBorder(graphics, ((LayoutElement) renderable).getRectangle(), 1, 0x88FF0000, true);
			}
		}
		graphics.disableScissor();

		if (pendingProgress != null) {
			drawBoxProgressBar(graphics, pendingProgress);
		}

		if (root && tooltip.sneakyDetails) {
			IThemeHelper.get().theme().sneakyDetails.render(graphics, partialTicks, this);
		}
	}

	@Override
	public void renderDebug(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, RenderDebugContext context) {
		super.renderDebug(graphics, mouseX, mouseY, partialTicks, context);
		if (!context.renderChildren) {
			return;
		}
		JadeUI.visitChildrenRecursive(
				layout, layoutElement -> {
					if (layoutElement instanceof Element element) {
						element.renderDebug(graphics, mouseX, mouseY, partialTicks, context);
					} else if (layoutElement instanceof Layout) {
						JadeInternals.getDisplayHelper().drawBorder(graphics, layoutElement.getRectangle(), 1, 0x8800FF00, true);
					}
				});
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}

	@Override
	public void setBoxProgress(MessageType type, float progress) {
		providers.removeIf(entry -> entry.priority() == STATIC_PROGRESS_PRIORITY);
		providers.add(new PrioritizedProvider(STATIC_PROGRESS_PRIORITY, new StaticBoxProgressProvider(progress, type)));
		providers.sort(Comparator.comparingInt(PrioritizedProvider::priority));
	}

	@Override
	public void clearBoxProgress() {
		providers.removeIf(entry -> entry.provider() instanceof StaticBoxProgressProvider);
	}

	@Override
	public void addProgressProvider(int priority, IBoxProgressProvider provider) {
		Objects.requireNonNull(provider);
		providers.add(new PrioritizedProvider(priority, provider));
		providers.sort(Comparator.comparingInt(PrioritizedProvider::priority));
	}

	public void beforeRender(Accessor<?> accessor, float partialTicks) {
		computeBoxProgress(accessor, partialTicks);
		for (Renderable renderable : renderables) {
			if (renderable instanceof BoxElementImpl box) {
				box.beforeRender(accessor, partialTicks);
			}
		}
	}

	private void computeBoxProgress(Accessor<?> accessor, float partialTicks) {
		BoxProgress current = null;
		for (PrioritizedProvider entry : providers) {
			BoxProgress progress = entry.provider().getProgress(this, accessor, partialTicks);
			if (progress != null) {
				current = progress;
				break;
			}
		}
		if (current != null) {
			fadeProgress = current;
			fadeAlpha = current.alpha();
			pendingProgress = current;
			BoxProgressFadeTrackInfo track = getFadeTrack(true);
			if (track != null) {
				track.progress = fadeProgress;
				track.alpha = fadeAlpha;
				track.touch();
			}
			return;
		}
		BoxProgressFadeTrackInfo track = getFadeTrack(false);
		if (track != null && fadeProgress == null) {
			fadeProgress = track.progress;
			fadeAlpha = track.alpha;
		}
		if (fadeProgress == null) {
			pendingProgress = null;
			return;
		}
		fadeAlpha -= Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() * 0.1F;
		if (fadeAlpha <= 0) {
			fadeAlpha = 0;
			fadeProgress = null;
			pendingProgress = null;
		} else {
			pendingProgress = new BoxProgress(fadeProgress.progress(), fadeProgress.type(), fadeProgress.color(), fadeAlpha);
		}
		if (track != null) {
			track.progress = fadeProgress;
			track.alpha = fadeAlpha;
			if (fadeProgress != null) {
				track.touch();
			}
		}
	}

	private @Nullable BoxProgressFadeTrackInfo getFadeTrack(boolean create) {
		if (fadeTrack != null) {
			return fadeTrack;
		}
		Identifier tag = getTag();
		if (tag == null) {
			return null;
		}
		ProgressTracker tracker = JadeClient.tickHandler().progressTracker;
		fadeTrack = tracker.get(tag, BoxProgressFadeTrackInfo.class);
		if (fadeTrack == null && create) {
			fadeTrack = tracker.getOrCreate(tag, BoxProgressFadeTrackInfo.class, BoxProgressFadeTrackInfo::new);
		}
		return fadeTrack;
	}

	private void drawBoxProgressBar(GuiGraphicsExtractor graphics, BoxProgress progress) {
		int baseColor = progress.color() != null ? progress.color() : style.boxProgressColors.get(progress.type());
		float boxAlpha = IDisplayHelper.get().backgroundOpacity();
		if (JadeIds.ROOT.equals(getTag())) {
			boxAlpha *= IWailaConfig.get().overlay().getAlpha();
		}
		int color = Overlay.applyAlpha(baseColor, progress.alpha() * boxAlpha);
		float x = getX();
		float top = getY() + getHeight();
		float width = getWidth();
		float offset0 = style.boxProgressOffset(ScreenDirection.UP);
		float offset1 = style.boxProgressOffset(ScreenDirection.RIGHT);
		float offset2 = style.boxProgressOffset(ScreenDirection.DOWN);
		float offset3 = style.boxProgressOffset(ScreenDirection.LEFT);
		width += offset1 - offset3;
		DisplayHelper.fill(
				graphics,
				x + offset3,
				top - 1 + offset0,
				x + offset3 + width * Mth.clamp(progress.progress(), 0, 1),
				top + offset2,
				color);
	}

	private record PrioritizedProvider(int priority, IBoxProgressProvider provider) {
	}

	@Override
	public @Nullable Element getIcon() {
		return icon;
	}

	@Override
	public void setIcon(@Nullable Element icon) {
		this.icon = icon;
	}

	public void updateExpectedRect(TooltipAnimation animation) {
		Window window = Minecraft.getInstance().getWindow();
		IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
		IWailaConfig.Accessibility accessibility = IWailaConfig.get().accessibility();
		float x = window.getGuiScaledWidth() * accessibility.tryFlip(overlay.getOverlayPosX());
		float y = window.getGuiScaledHeight() * (1.0F - overlay.getOverlayPosY());
		float width = layout.getWidth();
		float height = layout.getHeight();

		animation.scale = overlay.getOverlayScale();
		float thresholdHeight = window.getGuiScaledHeight() * overlay.getAutoScaleThreshold();
		if (!JadeUI.isPinned() && layout.getHeight() * animation.scale > thresholdHeight) {
			animation.scale = Math.max(animation.scale * 0.5f, thresholdHeight / layout.getHeight());
		}

		Rect2f expectedRect = animation.expectedRect;
		expectedRect.setWidth((int) (width * animation.scale));
		expectedRect.setHeight((int) (height * animation.scale));
		expectedRect.setX((int) (x - expectedRect.getWidth() * accessibility.tryFlip(overlay.getAnchorX())));
		expectedRect.setY((int) (y - expectedRect.getHeight() * overlay.getAnchorY()));

		if (PreviewOptionsScreen.isAdjustingPosition()) {
			return;
		}

		IWailaConfig.BossBarOverlapMode mode = IWailaConfig.get().general().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.PUSH_DOWN) {
			Rect2f bossBarRect = ClientProxy.getBossBarRect();
			// check if tooltip intersects with boss bar
			if (bossBarRect != null && bossBarRect.intersects(expectedRect)) {
				expectedRect.setY(bossBarRect.getY() + bossBarRect.getHeight());
			}
		}
	}

	public void updateRect(TooltipAnimation animation) {
		Rect2f src = animation.rect;
		Rect2f target = animation.expectedRect;
		if (src.getWidth() == 0) {
			src.setX(target.getX());
			src.setY(target.getY());
			src.setWidth(target.getWidth());
			src.setHeight(target.getHeight());
			animation.alpha = animation.showHideAlpha;
		} else {
			Duration duration = Duration.ofMillis(75);
			long deltaTime = System.currentTimeMillis() - animation.startTime;
			long durationMillis = duration.toMillis();
			float progress = (float) deltaTime / durationMillis;
			//noinspection MathClampMigration
			animation.alpha = Math.min(animation.showHideAlpha, Math.max(progress, 0.55F));
			chase(animation, Rect2f::getX, src::setX, progress);
			chase(animation, Rect2f::getY, src::setY, progress);
			chase(
					animation, Rect2f::getWidth, it -> {
						src.setWidth(it);
						width = (int) (it / animation.scale);
					}, progress);
			chase(
					animation, Rect2f::getHeight, it -> {
						src.setHeight(it);
						height = (int) (it / animation.scale);
					}, progress);
		}
	}

	@Override
	public BoxStyle getStyle() {
		return style;
	}

	@Override
	public @Nullable Component getNarration() {
		if (tooltip.isEmpty()) {
			return null;
		}
		String narration = tooltip.getNarration();
		if (narration.isEmpty()) {
			return null;
		}
		return Component.literal(narration);
	}

	@Override
	public void setFreeSpace(int width, int height) {
		layout.setFreeSpace(width, height);
		this.width = layout.getWidth();
		this.height = layout.getHeight();
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer) {
		layout.visitWidgets(consumer);
	}

	public void setWidgetAlpha(float alpha) {
		if (widgets == null) {
			ImmutableList.Builder<AbstractWidget> builder = ImmutableList.builder();
			visitWidgets(builder::add);
			widgets = builder.build();
		}
		for (AbstractWidget widget : widgets) {
			widget.setAlpha(alpha);
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		if (eventListeners == null) {
			ImmutableList.Builder<GuiEventListener> builder = ImmutableList.builder();
			for (Renderable renderable : renderables) {
				if (renderable instanceof GuiEventListener listener) {
					builder.add(listener);
				}
			}
			eventListeners = builder.build();
		}
		return eventListeners;
	}

	@Override
	public boolean isDragging() {
		return false;
	}

	@Override
	public void setDragging(boolean bl) {
	}

	@Override
	public @Nullable GuiEventListener getFocused() {
		return null;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener guiEventListener) {
	}
}
