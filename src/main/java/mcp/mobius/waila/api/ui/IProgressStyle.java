package mcp.mobius.waila.api.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.ITextComponent;

public interface IProgressStyle {

	default IProgressStyle color(int color) {
		return color(color, color);
	}

	IProgressStyle color(int color, int color2);

	IProgressStyle textColor(int color);

	IProgressStyle vertical(boolean vertical);

	IProgressStyle overlay(IElement overlay);

	void render(MatrixStack matrixStack, float x, float y, float w, float h, ITextComponent text);
}
