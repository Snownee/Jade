package snownee.jade.api.ui;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.ui.JadeUIInternal;

public final class JadeUI {
	private static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;

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
			Identifier baseSprite,
			Identifier progressSprite,
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

	public static ResizeableElement sprite(RenderPipeline renderPipeline, Identifier sprite, int width, int height) {
		return JadeUIInternal.sprite(renderPipeline, sprite, width, height);
	}

	public static ResizeableElement sprite(Identifier sprite, int width, int height) {
		return JadeUIInternal.sprite(sprite, width, height);
	}

	public static ResizeableElement horizontalTiledSprite(
			RenderPipeline renderPipeline,
			Identifier sprite,
			int width,
			int height) {
		return JadeUIInternal.horizontalTiledSprite(renderPipeline, sprite, width, height);
	}

	public static ResizeableElement verticalTiledSprite(RenderPipeline renderPipeline, Identifier sprite, int width, int height) {
		return JadeUIInternal.verticalTiledSprite(renderPipeline, sprite, width, height);
	}

	public static void visitChildrenRecursive(LayoutElement layoutElement, Consumer<LayoutElement> consumer) {
		JadeUIInternal.visitChildrenRecursive(layoutElement, consumer);
	}

	public static boolean isPinned() {
		return JadeUIInternal.isPinned();
	}

	public static boolean hasControlDown() {
		if (ON_OSX) {
			return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 343) ||
					InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 347);
		}
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 341) ||
				InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 345);
	}

	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340) ||
				InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
	}

	public static boolean hasAltDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 342) ||
				InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 346);
	}

	public static boolean hasTranslation(String key) {
		return Language.getInstance().has(key);
	}
}