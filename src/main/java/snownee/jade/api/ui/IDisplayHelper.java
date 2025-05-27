package snownee.jade.api.ui;

import java.text.Format;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import snownee.jade.JadeInternals;

public interface IDisplayHelper {

	static IDisplayHelper get() {
		return JadeInternals.getDisplayHelper();
	}

	void drawItem(GuiGraphics graphics, float x, float y, ItemStack stack, float scale, @Nullable String text);

	void drawGradientRect(GuiGraphics graphics, float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(GuiGraphics graphics, ScreenRectangle rectangle, int width, int color, boolean corner);

	String humanReadableNumber(double number, String unit, boolean milli);

	String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter);

	void drawText(GuiGraphics graphics, String text, float x, float y, int color);

	void drawText(GuiGraphics graphics, FormattedText text, float x, float y, int color);

	void drawText(GuiGraphics graphics, FormattedCharSequence text, float x, float y, int color);

	MutableComponent stripColor(Component component);

	void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l);

	void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l,
			int m);

	void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l,
			int m,
			int n,
			int o,
			int p);

	float opacity();
}
