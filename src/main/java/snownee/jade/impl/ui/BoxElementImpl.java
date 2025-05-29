package snownee.jade.impl.ui;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import snownee.jade.JadeInternals;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.gui.JadeLinearLayout;
import snownee.jade.gui.LayoutWithPadding;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.gui.ResizeableLayout;
import snownee.jade.impl.Tooltip;
import snownee.jade.track.ProgressTrackInfo;
import snownee.jade.util.ClientProxy;

public class BoxElementImpl extends BoxElement {
	public LayoutWithPadding layout;
	private final Tooltip tooltip;
	private final BoxStyle style;
	private final List<Renderable> renderables;
	private int[] padding;
	private Element icon;
	private float boxProgress;
	private MessageType boxProgressType;
	private ProgressTrackInfo track;

	public BoxElementImpl(Tooltip tooltip, BoxStyle style) {
		this.tooltip = Objects.requireNonNull(tooltip);
		this.style = Objects.requireNonNull(style);
		this.icon = tooltip.getIcon();
		arrangeElements();
		renderables = Lists.newArrayListWithExpectedSize(tooltip.size() + 1);
		JadeUI.visitChildrenRecursive(
				layout, element -> {
					if (element instanceof Renderable renderable) {
						renderables.add(renderable);
					}
				});
	}

	private void arrangeElements() {
		JadeLinearLayout linearLayout = JadeLinearLayout.vertical().alignItems(JadeLinearLayout.Align.STRETCH);
		for (Tooltip.Line line : tooltip.lines) {
			JadeLinearLayout lineLayout = JadeLinearLayout.horizontal();
			for (LayoutElement element : line.elements()) {
				if (element instanceof ResizeableLayout resizeableLayout) {
					lineLayout.addChild(
							element, lineLayout.newChildLayoutSettings(), container -> {
								container.flexGrow = resizeableLayout.getFlexGrow();
							});
				} else {
					lineLayout.addChild(element);
				}
			}
			linearLayout.addChild(
					lineLayout, linearLayout.newChildLayoutSettings(), container -> {
						container.headMargin = line.marginTop;
						container.tailMargin = line.marginBottom;
					});
		}

		if (icon != null) {
			JadeLinearLayout iconLayout = JadeLinearLayout.horizontal().alignItems(JadeLinearLayout.Align.START).spacing(3);
			if (IWailaConfig.get().overlay().getIconMode() == IWailaConfig.IconMode.CENTERED) {
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

	private static void chase(TooltipRect rect, ToIntFunction<Rect2i> getter, IntConsumer setter) {
		if (IWailaConfig.get().overlay().getAnimation()) {
			int source = getter.applyAsInt(rect.rect);
			int target = getter.applyAsInt(rect.expectedRect);
			float diff = target - source;
			if (diff == 0) {
				return;
			}
			float delta = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 2;
			if (delta == 0) {
				diff = diff > 0 ? 1 : -1;
			} else {
				if (delta < 1) {
					diff *= delta;
				}
				if (Mth.abs(diff) < 1) {
					diff = diff > 0 ? 1 : -1;
				}
			}
			setter.accept((int) (source + diff));
		} else {
			setter.accept(getter.applyAsInt(rect.expectedRect));
		}
	}

//	@Override
//	public Vec2 getSize() {
//		if (tooltip.isEmpty()) {
//			return Vec2.ZERO;
//		}
//		float width = 0, height = 0;
//		int lineCount = tooltip.lines.size();
//		Tooltip.Line line = tooltip.lines.getFirst();
//		for (int i = 0; i < lineCount; i++) {
//			Vec2 size = line.size();
//			width = Math.max(width, size.x);
//			height += size.y;
//			if (i < lineCount - 1) {
//				int marginBottom = line.marginBottom;
//				line = tooltip.lines.get(i + 1);
//				height += calculateMargin(marginBottom, line.marginTop);
//			}
//		}
//		contentSize = new Vec2(width, height);
//		if (icon != null) {
//			Vec2 size = icon.getCachedSize();
//			width += size.x + 3;
//			height = Math.max(height, size.y);
//		}
//		int twoOfBorder = style.borderWidth() * 2;
//		width += padding(ScreenDirection.LEFT) + padding(ScreenDirection.RIGHT) + twoOfBorder;
//		height += padding(ScreenDirection.UP) + padding(ScreenDirection.DOWN) + twoOfBorder;
//		// our limited negative-padding support:
//		width = Math.max(width, 0);
//		height = Math.max(height, 0);
//
//		if (icon != null && icon.getCachedSize().y > contentSize.y) {
//			setPadding(ScreenDirection.UP, padding(ScreenDirection.UP) + (int) (icon.getCachedSize().y - contentSize.y) / 2);
//		}
//
//		return new Vec2(width, height);
//	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (tooltip.isEmpty()) {
			return;
		}

		// render background
		float alpha = IDisplayHelper.get().opacity();
		if (JadeIds.ROOT.equals(getTag())) {
			alpha *= IWailaConfig.get().overlay().getAlpha();
		}
		if (alpha > 0) {
			style.render(graphics, this, getX(), getY(), getWidth(), getHeight(), alpha);
		}

		for (Renderable renderable : renderables) {
			renderable.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void renderDebug(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, RenderDebugContext context) {
		super.renderDebug(graphics, mouseX, mouseY, partialTicks, context);
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
	@Nullable
	public Element getIcon() {
		return icon;
	}

	@Override
	public void setIcon(@Nullable Element icon) {
		this.icon = icon;
	}

	public void updateExpectedRect(TooltipRect rect) {
		Window window = Minecraft.getInstance().getWindow();
		IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
		IWailaConfig.Accessibility accessibility = IWailaConfig.get().accessibility();
		float x = window.getGuiScaledWidth() * accessibility.tryFlip(overlay.getOverlayPosX());
		float y = window.getGuiScaledHeight() * (1.0F - overlay.getOverlayPosY());
		float width = getWidth();
		float height = getHeight();

		rect.scale = overlay.getOverlayScale();
		float thresholdHeight = window.getGuiScaledHeight() * overlay.getAutoScaleThreshold();
		if (getHeight() * rect.scale > thresholdHeight) {
			rect.scale = Math.max(rect.scale * 0.5f, thresholdHeight / getHeight());
		}

		Rect2i expectedRect = rect.expectedRect;
		expectedRect.setWidth((int) (width * rect.scale));
		expectedRect.setHeight((int) (height * rect.scale));
		expectedRect.setX((int) (x - expectedRect.getWidth() * accessibility.tryFlip(overlay.getAnchorX())));
		expectedRect.setY((int) (y - expectedRect.getHeight() * overlay.getAnchorY()));

		if (PreviewOptionsScreen.isAdjustingPosition()) {
			return;
		}

		IWailaConfig.BossBarOverlapMode mode = IWailaConfig.get().general().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.PUSH_DOWN) {
			Rect2i bossBarRect = ClientProxy.getBossBarRect();
			if (bossBarRect != null) {
				width = expectedRect.getWidth();
				height = expectedRect.getHeight();
				int rw = bossBarRect.getWidth();
				int rh = bossBarRect.getHeight();
				x = expectedRect.getX();
				y = expectedRect.getY();
				int rx = bossBarRect.getX();
				int ry = bossBarRect.getY();
				rw += rx;
				rh += ry;
				width += x;
				height += y;
				// check if tooltip intersects with boss bar
				if (rw > x && rh > y && width > rx && height > ry) {
					expectedRect.setY(bossBarRect.getHeight());
				}
			}
		}
	}

	public void updateRect(TooltipRect rect) {
		Rect2i src = rect.rect;
		if (src.getWidth() == 0) {
			src.setX(rect.expectedRect.getX());
			src.setY(rect.expectedRect.getY());
			src.setWidth(rect.expectedRect.getWidth());
			src.setHeight(rect.expectedRect.getHeight());
		} else {
			chase(rect, Rect2i::getX, src::setX);
			chase(rect, Rect2i::getY, src::setY);
			chase(rect, Rect2i::getWidth, src::setWidth);
			chase(rect, Rect2i::getHeight, src::setHeight);
		}
	}

	@Override
	public int padding(ScreenDirection direction) {
		if (padding != null) {
			return padding[direction.ordinal()];
		}
		return style.padding(direction);
	}

	@Override
	public void setPadding(ScreenDirection direction, int value) {
		if (padding == null) {
			padding = style.padding.clone();
		}
		padding[direction.ordinal()] = value;
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
}
