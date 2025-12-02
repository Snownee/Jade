package snownee.jade.impl.ui;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import snownee.jade.JadeInternals;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
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
import snownee.jade.track.ProgressTrackInfo;
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
	private float boxProgress;
	private @Nullable MessageType boxProgressType;
	private @Nullable ProgressTrackInfo track;

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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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
				renderable.render(graphics, mouseX, mouseY, partialTicks);
			} catch (Exception e) {
				WailaExceptionHandler.handleErr(e, null, null);
				IDisplayHelper.get().drawBorder(graphics, ((LayoutElement) renderable).getRectangle(), 1, 0x88FF0000, true);
			}
		}
		graphics.disableScissor();

		if (root && tooltip.sneakyDetails) {
			IThemeHelper.get().theme().sneakyDetails.render(graphics, partialTicks, this);
		}
	}

	@Override
	public void renderDebug(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, RenderDebugContext context) {
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

	//	@Override
//	public void render(GuiGraphics guiGraphics, final float x, final float y, final float maxX, final float maxY) {
//		if (tooltip.isEmpty()) {
//			return;
//		}
//		guiGraphics.pose().pushMatrix();
//		guiGraphics.pose().translate(x, y);
//
//		// render background
//		float alpha = IDisplayHelper.get().opacity();
//		if (JadeIds.ROOT.equals(getTag())) {
//			alpha *= IWailaConfig.get().overlay().getAlpha();
//		}
//		if (alpha > 0) {
//			style.render(guiGraphics, this, 0, 0, maxX - x, maxY - y, alpha);
//		}
//
//		int borderWidth = style.borderWidth();
//		// render box progress
//		if (boxProgressType != null) {
//			float left = style.boxProgressOffset(ScreenDirection.LEFT) + borderWidth;
//			float width = maxX - x - left;
//			float top = maxY - y - 1 + style.boxProgressOffset(ScreenDirection.UP) + borderWidth;
//			float height = 1 + style.boxProgressOffset(ScreenDirection.DOWN);
//			float progress = boxProgress;
//			if (track == null && tag != null) {
//				track = WailaTickHandler.instance().progressTracker.getOrCreate(
//						tag, ProgressTrackInfo.class, () -> {
//							return new ProgressTrackInfo(false, boxProgress, 0);
//						});
//			}
//			if (track != null) {
//				track.setProgress(progress);
//				track.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
//				progress = track.getSmoothProgress();
//			}
//			((DisplayHelper) IDisplayHelper.get()).drawGradientProgress(
//					guiGraphics,
//					left,
//					top,
//					width,
//					height,
//					progress,
//					style.boxProgressColors.get(boxProgressType));
//		}
//
//		float contentLeft = padding(ScreenDirection.LEFT) + borderWidth;
//		float contentTop = padding(ScreenDirection.UP) + borderWidth;
//
//		// render icon
//		if (icon != null) {
//			Vec2 iconSize = icon.getCachedSize();
//			Vec2 offset = icon.getTranslation();
//			float offsetY = offset.y;
//			float min = contentTop + padding(ScreenDirection.DOWN) + iconSize.y;
//			IWailaConfig.IconMode iconMode = IWailaConfig.get().overlay().getIconMode();
//			if (iconMode == IWailaConfig.IconMode.TOP && min < getCachedSize().y) {
//				offsetY += contentTop;
//			} else {
//				offsetY += (size.y - iconSize.y) / 2;
//			}
//			float offsetX = contentLeft + offset.x;
//			icon.render(guiGraphics, offsetX, offsetY, offsetX + iconSize.x, offsetY + iconSize.y);
//			contentLeft += iconSize.x + 3;
//		}
//
//		// render elements
//		{
//			boolean fancy = Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST;
//			if (fancy) {
//				guiGraphics.enableScissor(0, 0, (int) (maxX - x), (int) (maxY - y));
//			}
//			float lineTop = contentTop;
//			int lineCount = tooltip.lines.size();
//			Tooltip.Line line = tooltip.lines.getFirst();
//			for (int i = 0; i < lineCount; i++) {
//				Vec2 lineSize = line.size();
//				line.render(guiGraphics, contentLeft, lineTop, maxX - x - padding(ScreenDirection.RIGHT), lineTop + lineSize.y);
//				if (i < lineCount - 1) {
//					int marginBottom = line.marginBottom;
//					line = tooltip.lines.get(i + 1);
//					lineTop += lineSize.y + calculateMargin(marginBottom, line.marginTop);
//				}
//			}
//			if (fancy) {
//				guiGraphics.disableScissor();
//			}
//		}
//
//		// render down arrow
//		if (tooltip.sneakyDetails) {
//			float arrowTop = (OverlayRenderer.ticks / 5) % 8 - 2;
//			if (arrowTop <= 4) {
//				alpha = 1 - Math.abs(arrowTop) / 2;
//				if (alpha > 0.016) {
//					guiGraphics.pose().pushMatrix();
//					arrowTop += size.y - 6;
//					float arrowLeft = contentLeft + (contentSize.x - DisplayHelper.font().width("▾") + 1) / 2f;
//					guiGraphics.pose().translate(arrowLeft, arrowTop);
//					int color = Overlay.applyAlpha(IThemeHelper.get().theme().text.colors().info(), alpha);
//					DisplayHelper.INSTANCE.drawText(guiGraphics, "▾", 0, 0, color);
//					guiGraphics.pose().popMatrix();
//				}
//			}
//		}
//
//		guiGraphics.pose().popMatrix();
//	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}

	@Override
	public void setBoxProgress(MessageType type, float progress) {
		boxProgress = progress;
		boxProgressType = type;
	}

	@Override
	public float getBoxProgress() {
		return boxProgressType == null ? Float.NaN : boxProgress;
	}

	@Override
	public void clearBoxProgress() {
		boxProgress = 0;
		boxProgressType = null;
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
