package snownee.jade.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.overlay.DisplayHelper;

public class ProgressStyle implements IProgressStyle {

	public boolean autoTextColor = true;
	public int color;
	public int color2;
	public int textColor;
	public boolean vertical;
	public IElement overlay;
	public boolean glowText;
	public boolean shadow = true;

	public ProgressStyle() {
		color(0xFFFFFFFF);
	}

	@Override
	public IProgressStyle color(int color, int color2) {
		this.color = color;
		this.color2 = color2;
		return this;
	}

	@Override
	public IProgressStyle vertical(boolean vertical) {
		this.vertical = vertical;
		return this;
	}

	@Override
	public IProgressStyle overlay(IElement overlay) {
		this.overlay = overlay;
		return this;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float width, float height, float progress, Component text) {
		progress *= choose(true, width, height);
		float progressY = y;
		if (vertical) {
			progressY += height - progress;
		}
		if (progress > 0) {
			if (overlay != null) {
				Vec2 size = new Vec2(choose(true, progress, width), choose(false, progress, height));
				overlay.size(size);
				overlay.render(matrixStack, x, progressY, size.x, size.y);
			} else {
				int alpha = (int) (((color >> 24) & 0xFF) * 0.7f);
				int lighter = (color & 0xFFFFFF) | alpha << 24;

				float half = choose(true, height, width) / 2;
				DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, progressY, choose(true, progress, half), choose(false, progress, half), lighter, color, vertical);
				DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + choose(false, half, 0), progressY + choose(true, half, 0), choose(true, progress, half), choose(false, progress, half), color, lighter, vertical);
				if (color != color2) {
					if (vertical) {
						for (float yy = y + height; yy > progressY; yy -= 2) {
							float fy = Math.max(progressY, yy + 1);
							DisplayHelper.fill(matrixStack, x, yy, x + width, fy, color2);
						}
					} else {
						for (float xx = x + 1; xx < x + progress; xx += 2) {
							float fx = Math.min(x + width, xx + 1);
							DisplayHelper.fill(matrixStack, xx, y, fx, y + height, color2);
						}
					}
				}
			}
		}
		if (text != null) {
			Font font = Minecraft.getInstance().font;
			if (autoTextColor) {
				autoTextColor = false;
				if (overlay == null && RGBtoHSV(color2).z() > 0.75f) {
					textColor = 0xFF000000;
					shadow = false;
				} else {
					textColor = 0xFFFFFFFF;
				}
			}
			y += height - font.lineHeight;
			if (vertical && font.lineHeight < progress) {
				y -= progress;
				y += font.lineHeight + 2;
			}
			if (glowText) {
				MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				font.drawInBatch8xOutline(text.getVisualOrderText(), x + 1, y, 0xFFFFFFFF, 0xFF333333, matrixStack.last().pose(), multibuffersource$buffersource, 15728880);
				multibuffersource$buffersource.endBatch();
			} else if (shadow) {
				font.drawShadow(matrixStack, text, x + 1, y, textColor);
			} else {
				font.draw(matrixStack, text, x + 1, y, textColor);
			}
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

	public IProgressStyle glowText(boolean glowText) {
		this.glowText = glowText;
		return this;
	}

}
