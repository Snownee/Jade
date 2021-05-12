package mcp.mobius.waila.impl.ui;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

public class ProgressStyle implements IProgressStyle {

	public boolean autoTextColor = true;
	public int color;
	public int color2;
	public int textColor;
	public boolean vertical;
	public IElement overlay;

	@Override
	public IProgressStyle color(int color, int color2) {
		this.color = color;
		this.color2 = color2;
		return this;
	}

	@Override
	public IProgressStyle vertical(boolean vertical) { //TODO
		this.vertical = vertical;
		return this;
	}

	@Override
	public IProgressStyle overlay(IElement overlay) {
		this.overlay = overlay;
		return this;
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float width, float height, float progress, ITextComponent text) {
		progress *= choose(true, width, height);
		float progressY = y;
		if (vertical) {
			progressY += height - progress;
		}
		if (progress > 0) {
			if (overlay != null) {
				Vector2f size = new Vector2f(choose(true, progress, width), choose(false, progress, height));
				overlay.size(size);
				overlay.render(matrixStack, x, progressY, size.x, size.y);
			} else {
				Color lighter = new Color(color);
				int alpha = (int) (lighter.getAlpha() * 0.7f);
				lighter = new Color(lighter.getRed(), lighter.getGreen(), lighter.getBlue(), alpha);

				float half = choose(true, height, width) / 2;
				DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, progressY, choose(true, progress, half), choose(false, progress, half), lighter.getRGB(), color, vertical);
				DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + choose(false, half, 0), progressY + choose(true, half, 0), choose(true, progress, half), choose(false, progress, half), color, lighter.getRGB(), vertical);
				if (color != color2) {
					if (vertical) {
						for (float yy = y + height; yy > progressY; yy -= 2) {
							float fy = Math.max(progressY, yy + 1);
							DisplayHelper.fill(matrixStack, x, yy, x + width, fy, color2);
						}
					} else {
						for (float xx = x + 1; xx < x + width; xx += 2) {
							float fx = Math.min(x + width, xx + 1);
							DisplayHelper.fill(matrixStack, xx, y, fx, y + height, color2);
						}
					}
				}
			}
		}
		if (text != null) {
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			if (autoTextColor) {
				autoTextColor = false;
				Vector3f hsv = RGBtoHSV(color2);
				if (hsv.getZ() > 0.5f) {
					textColor = 0xFF000000;
				} else {
					textColor = 0xFFFFFFFF;
				}
			}
			y += height - font.FONT_HEIGHT;
			if (vertical && font.FONT_HEIGHT < progress) {
				y -= progress;
				y += font.FONT_HEIGHT + 2;
			}
			font.drawStringWithShadow(matrixStack, text.getString(), x + 1, y, textColor);
		}
	}

	private float choose(boolean expand, float x, float y) {
		return vertical ^ expand ? x : y;
	}

	private static Vector3f RGBtoHSV(int rgb) {
		int r = (rgb >> 16) & 255;
		int g = (rgb >> 8) & 255;
		int b = rgb & 255;
		int max = Math.max(r, Math.max(g, b));
		int min = Math.min(r, Math.min(g, b));
		float v = max;
		float delta = max - min;
		float h, s;
		if (max != 0)
			s = delta / max; // s
		else {
			// r = g = b = 0        // s = 0, v is undefined
			s = 0;
			h = -1;
			return new Vector3f(h, s, 0 /*Float.NaN*/);
		}
		if (r == max)
			h = (g - b) / delta; // between yellow & magenta
		else if (g == max)
			h = 2 + (b - r) / delta; // between cyan & yellow
		else
			h = 4 + (r - g) / delta; // between magenta & cyan
		h /= 6; // degrees
		if (h < 0)
			h += 1;
		return new Vector3f(h, s, v / 255);
	}

	@Override
	public IProgressStyle textColor(int color) {
		textColor = color;
		autoTextColor = false;
		return this;
	}

}
