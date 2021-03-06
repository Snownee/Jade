package mcp.mobius.waila.api.ui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.item.ItemStack;

public interface IDisplayHelper {

	void drawItem(MatrixStack matrixStack, float x, float y, ItemStack stack, float scale, @Nullable String text);

	void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(MatrixStack matrixStack, float minX, float minY, float maxX, float maxY, IBorderStyle border);

	String humanReadableNumber(double number, String unit, boolean milli);

}
