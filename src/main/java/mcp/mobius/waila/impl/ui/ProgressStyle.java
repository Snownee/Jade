package mcp.mobius.waila.impl.ui;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class ProgressStyle implements IProgressStyle {

	public boolean autoTextColor = true;
	public int color;
	public int color2;
	public int textColor;
	public boolean vertical;
	public FluidStack tempFluid;

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
	public IProgressStyle fluid(FluidStack fluidStack) {
		this.tempFluid = fluidStack;
		return this;
	}

	@Override
	public void render(MatrixStack matrixStack, int x, int y, int width, int height, ITextComponent text) {
		if (width > 0) {
			Color lighter = new Color(color);
			int alpha = (int) (lighter.getAlpha() * 0.7f);
			lighter = new Color(lighter.getRed(), lighter.getGreen(), lighter.getBlue(), alpha);

			float half = height / 2;
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y, width, half, lighter.getRGB(), color);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y + half, width, half, color, lighter.getRGB());
			if (color != color2) {
				for (int xx = x + 1; xx < x + width; xx += 2) {
					AbstractGui.fill(matrixStack, xx, y, xx + 1, y + height, color2);
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
			font.drawString(matrixStack, text.getString(), x + 1, y, textColor);
		}
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
