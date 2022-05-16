package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IDisplayHelper {

	void drawItem(PoseStack poseStack, float x, float y, ItemStack stack, float scale, @Nullable String text);

	void drawGradientRect(PoseStack poseStack, float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(PoseStack poseStack, float minX, float minY, float maxX, float maxY, IBorderStyle border);

	String humanReadableNumber(double number, String unit, boolean milli);

	void drawText(PoseStack poseStack, String text, float x, float y, int color);

	void drawText(PoseStack poseStack, Component text, float x, float y, int color);
}
