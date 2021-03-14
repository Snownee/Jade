package mcp.mobius.waila.api.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.item.ItemStack;

public interface IDisplayHelper {

    void drawItem(MatrixStack matrixStack, int x, int y, ItemStack stack, float scale);

    void drawGradientRect(MatrixStack matrixStack, int left, int top, int right, int bottom, int startColor, int endColor);

    void drawBorder(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, IBorderStyle border);

}
