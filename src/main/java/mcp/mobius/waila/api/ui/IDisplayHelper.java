package mcp.mobius.waila.api.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.item.ItemStack;

public interface IDisplayHelper {

	void drawItem(MatrixStack matrixStack, int x, int y, ItemStack stack, float scale);

	void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, IBorderStyle border);

	String humanReadableNumber(double number, String unit, boolean milli);

}
