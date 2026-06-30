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

/**
 * Static factory and utility methods for Jade UI elements.
 */
public final class JadeUI {
	private static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;

	/**
	 * Returns whether the given element is empty.
	 *
	 * @param element element to inspect
	 * @return {@code true} if the element renders nothing
	 */
	public static boolean isEmptyElement(@Nullable Element element) {
		return JadeUIInternal.isEmptyElement(element);
	}

	/**
	 * Creates a text element from a component.
	 *
	 * @param component text component
	 * @return text element
	 */
	public static TextElement text(Component component) {
		return JadeUIInternal.text(component);
	}

	/**
	 * Creates an empty spacer element.
	 *
	 * @param x width
	 * @param y height
	 * @return spacer element
	 */
	public static ResizeableElement spacer(int x, int y) {
		return JadeUIInternal.spacer(x, y);
	}

	/**
	 * Creates an item element.
	 *
	 * @param itemStack item to render
	 * @return item element
	 */
	public static Element item(ItemStack itemStack) {
		return item(itemStack, 1F);
	}

	/**
	 * Creates an item element.
	 *
	 * @param itemStack item to render
	 * @param scale render scale
	 * @return item element
	 */
	public static Element item(ItemStack itemStack, float scale) {
		return item(itemStack, scale, null);
	}

	/**
	 * Creates an item element with optional stack text.
	 *
	 * @param itemStack item to render
	 * @param scale render scale
	 * @param text optional overlay text
	 * @return item element
	 */
	public static Element item(ItemStack itemStack, float scale, @Nullable String text) {
		return JadeUIInternal.item(itemStack, scale, text);
	}

	/**
	 * Creates a small item element.
	 *
	 * @param itemStack item to render
	 * @return item element
	 */
	public static Element smallItem(ItemStack itemStack) {
		return JadeUIInternal.smallItem(itemStack);
	}

	/**
	 * Creates a fluid element.
	 *
	 * @param fluid fluid object
	 * @return fluid element
	 */
	public static ResizeableElement fluid(JadeFluidObject fluid) {
		return JadeUIInternal.fluid(fluid);
	}

	/**
	 * Creates a default progress arrow.
	 *
	 * @param progress progress value
	 * @return progress element
	 */
	public static Element progressArrow(float progress) {
		return JadeUIInternal.progressArrow(progress);
	}

	/**
	 * Creates a progress element.
	 *
	 * @param view progress view
	 * @return progress element
	 */
	public static ResizeableElement progress(ProgressView view) {
		return JadeUIInternal.progress(view);
	}

	/**
	 * Creates a progress element with explicit size.
	 *
	 * @param view progress view
	 * @param width width
	 * @param height height
	 * @return progress element
	 */
	public static ResizeableElement progress(ProgressView view, int width, int height) {
		return JadeUIInternal.progress(view, width, height);
	}

	/**
	 * Creates a configurable progress element.
	 *
	 * @param progress progress value
	 * @param baseSprite base sprite
	 * @param progressSprite fill sprite
	 * @param width width
	 * @param height height
	 * @param text optional label
	 * @param style optional style
	 * @return progress element
	 */
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
	 * Creates a nested tooltip box.
	 *
	 * @param tooltip nested tooltip
	 * @param boxStyle box style
	 * @return box element
	 */
	public static BoxElement box(ITooltip tooltip, BoxStyle boxStyle) {
		return JadeUIInternal.box(tooltip, boxStyle);
	}

	/**
	 * Creates an empty tooltip.
	 *
	 * @return empty tooltip
	 */
	public static ITooltip tooltip() {
		return tooltip(null);
	}

	/**
	 * Creates a tooltip with an optional icon.
	 *
	 * @param icon tooltip icon
	 * @return tooltip
	 */
	public static ITooltip tooltip(@Nullable Element icon) {
		return JadeUIInternal.tooltip(icon);
	}

	/**
	 * Returns the default progress style.
	 *
	 * @return progress style
	 */
	public static ProgressStyle progressStyle() {
		return JadeUIInternal.progressStyle();
	}

	/**
	 * Creates a sprite element.
	 *
	 * @param renderPipeline render pipeline
	 * @param sprite sprite identifier
	 * @param width width
	 * @param height height
	 * @return sprite element
	 */
	public static ResizeableElement sprite(RenderPipeline renderPipeline, Identifier sprite, int width, int height) {
		return JadeUIInternal.sprite(renderPipeline, sprite, width, height);
	}

	/**
	 * Creates a sprite element.
	 *
	 * @param sprite sprite identifier
	 * @param width width
	 * @param height height
	 * @return sprite element
	 */
	public static ResizeableElement sprite(Identifier sprite, int width, int height) {
		return JadeUIInternal.sprite(sprite, width, height);
	}

	/**
	 * Creates a horizontally tiled sprite element.
	 *
	 * @param renderPipeline render pipeline
	 * @param sprite sprite identifier
	 * @param width width
	 * @param height height
	 * @return sprite element
	 */
	public static ResizeableElement horizontalTiledSprite(
			RenderPipeline renderPipeline,
			Identifier sprite,
			int width,
			int height) {
		return JadeUIInternal.horizontalTiledSprite(renderPipeline, sprite, width, height);
	}

	/**
	 * Creates a vertically tiled sprite element.
	 *
	 * @param renderPipeline render pipeline
	 * @param sprite sprite identifier
	 * @param width width
	 * @param height height
	 * @return sprite element
	 */
	public static ResizeableElement verticalTiledSprite(RenderPipeline renderPipeline, Identifier sprite, int width, int height) {
		return JadeUIInternal.verticalTiledSprite(renderPipeline, sprite, width, height);
	}

	/**
	 * Visits an element and all of its children recursively.
	 *
	 * @param layoutElement root element
	 * @param consumer visitor callback
	 */
	public static void visitChildrenRecursive(LayoutElement layoutElement, Consumer<LayoutElement> consumer) {
		JadeUIInternal.visitChildrenRecursive(layoutElement, consumer);
	}

	/**
	 * Returns whether Jade UI is currently pinned.
	 *
	 * @return {@code true} if pinned
	 */
	public static boolean isPinned() {
		return JadeUIInternal.isPinned();
	}

	/**
	 * Returns whether the control key is pressed.
	 *
	 * @return {@code true} if control is down
	 */
	public static boolean hasControlDown() {
		if (ON_OSX) {
			return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 343) ||
					InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 347);
		}
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 341) ||
				InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 345);
	}

	/**
	 * Returns whether the shift key is pressed.
	 *
	 * @return {@code true} if shift is down
	 */
	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340) ||
				InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
	}

	/**
	 * Returns whether the alt key is pressed.
	 *
	 * @return {@code true} if alt is down
	 */
	public static boolean hasAltDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 342) ||
				InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 346);
	}

	/**
	 * Returns whether a translation exists for the given key.
	 *
	 * @param key translation key
	 * @return {@code true} if the key has a translation
	 */
	public static boolean hasTranslation(String key) {
		return Language.getInstance().has(key);
	}
}