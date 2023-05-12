package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import snownee.jade.overlay.DisplayHelper;

public interface IDisplayHelper {

	static IDisplayHelper get() {
		return DisplayHelper.INSTANCE;
	}

	void drawItem(PoseStack poseStack, float x, float y, ItemStack stack, float scale, @Nullable String text);

	void drawGradientRect(PoseStack poseStack, float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(PoseStack poseStack, float minX, float minY, float maxX, float maxY, float width, int color, boolean corner);

	String humanReadableNumber(double number, String unit, boolean milli);

	void drawText(PoseStack poseStack, String text, float x, float y, int color);

	void drawText(PoseStack poseStack, FormattedText text, float x, float y, int color);

	void drawText(PoseStack poseStack, FormattedCharSequence text, float x, float y, int color);

	MutableComponent stripColor(Component component);
}
