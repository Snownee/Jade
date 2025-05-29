package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.JadeInternals;
import snownee.jade.api.ITooltip;
import snownee.jade.api.fluid.JadeFluidObject;

public interface IElementHelper {

	static IElementHelper get() {
		return JadeInternals.getElementHelper();
	}

	boolean isEmptyElement(@Nullable Element element);

	TextElement text(Component component);

	ResizeableElement spacer(int x, int y);

	Element item(ItemStack itemStack);

	Element item(ItemStack itemStack, float scale);

	Element item(ItemStack itemStack, float scale, @Nullable String text);

	Element smallItem(ItemStack itemStack);

	Element fluid(JadeFluidObject fluid);

	Element progress(float progress, @Nullable Component text, ProgressStyle style, BoxStyle boxStyle, boolean canDecrease);

	Element progress(float progress);

	Element progress(
			float progress,
			ResourceLocation baseSprite,
			ResourceLocation progressSprite,
			int width,
			int height,
			boolean canDecrease);

	/**
	 * Display a nested tooltip
	 */
	BoxElement box(ITooltip tooltip, BoxStyle boxStyle);

	/**
	 * Create an empty tooltip. Used by the {@code box} method.
	 */
	default ITooltip tooltip() {
		return tooltip(null);
	}

	ITooltip tooltip(@Nullable Element icon);

	ProgressStyle progressStyle();

	Element sprite(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height);

	Element sprite(ResourceLocation sprite, int width, int height);

	ResizeableElement offset(Element element, int x, int y);

	ResizeableElement size(Element element, int width, int height);
}
