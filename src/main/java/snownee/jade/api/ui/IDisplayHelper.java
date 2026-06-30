package snownee.jade.api.ui;

import java.text.Format;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import snownee.jade.JadeInternals;

/**
 * Abstraction over platform-specific rendering helpers used by Jade UI elements.
 */
public interface IDisplayHelper {

	/**
	 * Returns the active helper instance.
	 *
	 * @return display helper
	 */
	static IDisplayHelper get() {
		return JadeInternals.getDisplayHelper();
	}

	/**
	 * Draws an item stack.
	 *
	 * @param graphics graphics context
	 * @param x x position
	 * @param y y position
	 * @param stack item stack
	 * @param scale render scale
	 * @param text optional overlay text
	 */
	void drawItem(GuiGraphicsExtractor graphics, float x, float y, ItemStack stack, float scale, @Nullable String text);

	default void drawBorder(GuiGraphicsExtractor graphics, ScreenRectangle rectangle, int width, int color, boolean corner) {
		drawBorder(graphics, new Rect2f(rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height()), width, color, corner);
	}

	void drawBorder(GuiGraphicsExtractor graphics, Rect2f rectangle, int width, int color, boolean corner);

	String humanReadableNumber(double number, String unit, boolean milli);

	String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter);

	void drawText(GuiGraphicsExtractor graphics, String text, float x, float y, int color);

	void drawText(GuiGraphicsExtractor graphics, FormattedText text, float x, float y, int color);

	void drawText(GuiGraphicsExtractor graphics, FormattedCharSequence text, float x, float y, int color);

	MutableComponent stripColor(Component component);

	void blitSprite(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier Identifier, int i, int j, int k, int l);

	void blitSprite(
			GuiGraphicsExtractor graphics,
			RenderPipeline renderPipeline,
			Identifier Identifier,
			int i,
			int j,
			int k,
			int l,
			int m);

	void blitSprite(
			GuiGraphicsExtractor graphics,
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
			GuiGraphicsExtractor graphics,
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
