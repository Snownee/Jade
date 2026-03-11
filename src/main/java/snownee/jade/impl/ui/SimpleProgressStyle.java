package snownee.jade.impl.ui;

import com.google.common.base.Preconditions;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.ui.ScreenDirection;

public class SimpleProgressStyle extends ProgressStyle {

	public boolean autoTextColor = true; // TODO
	public int color;
	public boolean vertical;

	@Override
	public ProgressStyle direction(ScreenDirection direction) {
		Preconditions.checkArgument(
				direction == ScreenDirection.UP || direction == ScreenDirection.RIGHT,
				"Only UP and RIGHT are supported");
		super.direction(direction);
		vertical = direction.isVertical();
		return this;
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, float x, float y, float width, float height, float progress, Component text) {
//		progress *= choose(true, width, height);
//		float progressY = y;
//		if (vertical) {
//			progressY += height - progress;
//		}
//		if (progress > 0) {
//			if (overlay != null) {
//				Vec2 size = new Vec2(choose(true, progress, width), choose(false, progress, height));
//				overlay.size(size);
//				overlay.render(guiGraphics, x, progressY, size.x, size.y);
//			} else {
//				Color color3 = Color.rgb(color);
//				int lighter = Color.hsl(color3.getHue(), color3.getSaturation(), color3.getLightness() * 0.7f, color3.getOpacity()).toInt();
//
//				float half = choose(true, height, width) / 2;
//				DisplayHelper.INSTANCE.drawGradientRect(
//						guiGraphics,
//						x,
//						progressY,
//						choose(true, progress, half),
//						choose(false, progress, half),
//						lighter,
//						color,
//						vertical);
//				DisplayHelper.INSTANCE.drawGradientRect(
//						guiGraphics,
//						x + choose(false, half, 0),
//						progressY + choose(true, half, 0),
//						choose(true, progress, half),
//						choose(false, progress, half),
//						color,
//						lighter,
//						vertical);
//				if (color != color2) {
//					if (vertical) {
//						for (float yy = y + height; yy > progressY; yy -= 2) {
//							float fy = Math.max(progressY, yy + 1);
//							DisplayHelper.fill(guiGraphics, x, yy, x + width, fy, color2);
//						}
//					} else {
//						for (float xx = x + 1; xx < x + progress; xx += 2) {
//							float fx = Math.min(x + width, xx + 1);
//							DisplayHelper.fill(guiGraphics, xx, y, fx, y + height, color2);
//						}
//					}
//				}
//			}
//		}
//		if (text != null) {
//			Font font = DisplayHelper.font();
//			if (autoTextColor) {
//				autoTextColor = false;
//				if (overlay == null && RGBtoHSV(color2).z() > 0.75f) {
//					textColor = 0xFF000000;
//				} else {
//					textColor = IThemeHelper.get().getNormalColor();
//				}
//			} else if (textColor == -1) {
//				textColor = IThemeHelper.get().getNormalColor();
//			}
//			y += height - font.lineHeight;
//			if (vertical && font.lineHeight < progress) {
//				y -= progress;
//				y += font.lineHeight + 2;
//			}
//			int color = Overlay.applyAlpha(textColor, OverlayRenderer.alpha);
//			int textWidth = font.width(text);
//			guiGraphics.fill(
//					(int) x,
//					(int) y - 2,
//					(int) x + textWidth + 1,
//					(int) y + font.lineHeight,
//					ARGB.as8BitChannel(IWailaConfig.get().accessibility().getTextBackgroundOpacity()) << 24);
//			guiGraphics.drawString(font, text, (int) x + 2, (int) y - 1, color);
//		}
	}

	private float choose(boolean expand, float x, float y) {
		return vertical ^ expand ? x : y;
	}

}
