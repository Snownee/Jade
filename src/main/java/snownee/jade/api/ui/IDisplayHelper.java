package snownee.jade.api.ui;

import java.text.Format;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import snownee.jade.Internals;

public interface IDisplayHelper {

	static IDisplayHelper get() {
		return Internals.getDisplayHelper();
	}

	void drawItem(GuiGraphics guiGraphics, float x, float y, ItemStack stack, float scale, @Nullable String text);

	void drawGradientRect(GuiGraphics guiGraphics, float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(GuiGraphics guiGraphics, float minX, float minY, float maxX, float maxY, float width, int color, boolean corner);

	String humanReadableNumber(double number, String unit, boolean milli);

	String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter);

	void drawText(GuiGraphics guiGraphics, String text, float x, float y, int color);

	void drawText(GuiGraphics guiGraphics, FormattedText text, float x, float y, int color);

	void drawText(GuiGraphics guiGraphics, FormattedCharSequence text, float x, float y, int color);

	MutableComponent stripColor(Component component);
}
