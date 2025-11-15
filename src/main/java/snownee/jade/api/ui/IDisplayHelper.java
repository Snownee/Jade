package snownee.jade.api.ui;

import java.text.Format;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import snownee.jade.JadeInternals;

public interface IDisplayHelper {

	static IDisplayHelper get() {
		return JadeInternals.getDisplayHelper();
	}

	void drawItem(GuiGraphics graphics, float x, float y, ItemStack stack, float scale, @Nullable String text);

	default void drawBorder(GuiGraphics graphics, ScreenRectangle rectangle, int width, int color, boolean corner) {
		drawBorder(graphics, new Rect2f(rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height()), width, color, corner);
	}

	void drawBorder(GuiGraphics graphics, Rect2f rectangle, int width, int color, boolean corner);

	String humanReadableNumber(double number, String unit, boolean milli);

	String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter);

	void drawText(GuiGraphics graphics, String text, float x, float y, int color);

	void drawText(GuiGraphics graphics, FormattedText text, float x, float y, int color);

	void drawText(GuiGraphics graphics, FormattedCharSequence text, float x, float y, int color);

	MutableComponent stripColor(Component component);

	void blitSprite(GuiGraphics graphics, RenderPipeline renderPipeline, Identifier Identifier, int i, int j, int k, int l);

	void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			Identifier Identifier,
			int i,
			int j,
			int k,
			int l,
			int m);

	void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			Identifier Identifier,
			int spriteWidth,
			int spriteHeight,
			int uStart,
			int vStart,
			int x,
			int y,
			int width,
			int height);

	void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			Identifier Identifier,
			int spriteWidth,
			int spriteHeight,
			int uStart,
			int vStart,
			int x,
			int y,
			int width,
			int height,
			int color);

	float opacity();

	float backgroundOpacity();
}
