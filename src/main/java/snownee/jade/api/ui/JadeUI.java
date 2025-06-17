package snownee.jade.api.ui;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.ui.JadeUIInternal;

public final class JadeUI {
	public static boolean isEmptyElement(@Nullable Element element) {
		return JadeUIInternal.isEmptyElement(element);
	}

	public static TextElement text(Component component) {
		return JadeUIInternal.text(component);
	}

	public static ResizeableElement spacer(int x, int y) {
		return JadeUIInternal.spacer(x, y);
	}

	public static Element item(ItemStack itemStack) {
		return item(itemStack, 1F);
	}

	public static Element item(ItemStack itemStack, float scale) {
		return item(itemStack, scale, null);
	}

	public static Element item(ItemStack itemStack, float scale, @Nullable String text) {
		return JadeUIInternal.item(itemStack, scale, text);
	}

	public static Element smallItem(ItemStack itemStack) {
		return JadeUIInternal.smallItem(itemStack);
	}

	public static ResizeableElement fluid(JadeFluidObject fluid) {
		return JadeUIInternal.fluid(fluid);
	}

	public static Element progressArrow(float progress) {
		return JadeUIInternal.progressArrow(progress);
	}

	public static ResizeableElement progress(ProgressView view) {
		return JadeUIInternal.progress(view);
	}

	public static ResizeableElement progress(ProgressView view, int width, int height) {
		return JadeUIInternal.progress(view, width, height);
	}

	public static ResizeableElement progress(
			float progress,
			ResourceLocation baseSprite,
			ResourceLocation progressSprite,
			int width,
			int height,
			@Nullable Component text,
			@Nullable ProgressStyle style) {
		return JadeUIInternal.progress(progress, baseSprite, progressSprite, width, height, text, style);
	}

	/**
	 * Display a nested tooltip
	 */
	public static BoxElement box(ITooltip tooltip, BoxStyle boxStyle) {
		return JadeUIInternal.box(tooltip, boxStyle);
	}

	/**
	 * Create an empty tooltip. Used by the {@code box} method.
	 */
	public static ITooltip tooltip() {
		return tooltip(null);
	}

	public static ITooltip tooltip(@Nullable Element icon) {
		return JadeUIInternal.tooltip(icon);
	}

	public static ProgressStyle progressStyle() {
		return JadeUIInternal.progressStyle();
	}

	public static ResizeableElement sprite(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height) {
		return JadeUIInternal.sprite(renderPipeline, sprite, width, height);
	}

	public static ResizeableElement sprite(ResourceLocation sprite, int width, int height) {
		return JadeUIInternal.sprite(sprite, width, height);
	}

	public static ResizeableElement horizontalTiledSprite(
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			int width,
			int height) {
		return JadeUIInternal.horizontalTiledSprite(renderPipeline, sprite, width, height);
	}

	public static ResizeableElement verticalTiledSprite(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height) {
		return JadeUIInternal.verticalTiledSprite(renderPipeline, sprite, width, height);
	}

	public static void visitChildrenRecursive(LayoutElement layoutElement, Consumer<LayoutElement> consumer) {
		JadeUIInternal.visitChildrenRecursive(layoutElement, consumer);
	}

	public static boolean isPinned() {
		return JadeUIInternal.isPinned();
	}
}