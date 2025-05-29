package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.ui.TextElement;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.Tooltip;
import snownee.jade.overlay.DisplayHelper;

public class JadeUIInternal {
	public static final ResourceLocation DEFAULT_PROGRESS = JadeIds.JADE("progress");
	public static final ResourceLocation DEFAULT_PROGRESS_BASE = JadeIds.JADE("progress_base");
	private static ResourceLocation uid;

	public static boolean isEmptyElement(Element element) {
		return element == null || element == ItemStackElement.EMPTY;
	}

	public static TextElement text(Component component) {
		return new TextElementImpl(component);
	}

	public static Element item(ItemStack stack) {
		return ItemStackElement.of(stack);
	}

	public static Element item(ItemStack stack, float scale) {
		return ItemStackElement.of(stack, scale);
	}

	public static Element item(ItemStack stack, float scale, String text) {
		return ItemStackElement.of(stack, scale, text);
	}

	public static Element smallItem(ItemStack stack) {
		int lineHeight = DisplayHelper.font().lineHeight;
		return item(stack, 0.5F, "").size(lineHeight + 1, lineHeight - 1).offset(0, -1).narration("");
	}

	public static Element fluid(JadeFluidObject fluid) {
		return new FluidStackElement(fluid);
	}

	public static SpacerElement spacer(int width, int height) {
		return new SpacerElement(width, height);
	}

	public static Element progressArrow(float progress) {
		return progress(progress, DEFAULT_PROGRESS_BASE, DEFAULT_PROGRESS, 22, 16, null, null);
	}

	public static ResizeableElement progress(ProgressView view) {
		return new ProgressElement(view);
	}

	public static ResizeableElement progress(
			float progress,
			ResourceLocation baseSprite,
			ResourceLocation progressSprite,
			int width,
			int height,
			@Nullable Component text,
			@Nullable ProgressStyle style) {
		return progress(new ProgressView(
				ProgressView.Part.of(progress, sprite(progressSprite, width, height)),
				text,
				style == null ? progressStyle() : style,
				BoxStyle.getSprite(baseSprite, null)));
	}

	public static BoxElement box(ITooltip tooltip, BoxStyle boxStyle) {
		return new BoxElementImpl((Tooltip) tooltip, boxStyle);
	}

	public static ITooltip tooltip(@Nullable Element icon) {
		Tooltip tooltip = new Tooltip();
		if (icon != null) {
			tooltip.setIcon(icon);
		}
		return tooltip;
	}

	public static ProgressStyle progressStyle() {
		return new SimpleProgressStyle();
	}

	public static Element sprite(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height) {
		return new SpriteElement(renderPipeline, sprite, width, height);
	}

	public static Element sprite(ResourceLocation sprite, int width, int height) {
		return new SpriteElement(sprite, width, height);
	}

	public static ResizeableElement offset(Element element, int x, int y) {
		return spacer(element.getWidth(), element.getHeight()).wrapped(element).offset(x, y);
	}

	public static ResizeableElement size(Element element, int width, int height) {
		return spacer(width, height).wrapped(element);
	}

	public static @Nullable ResourceLocation currentUid() {
		return uid;
	}

	public static void setCurrentUid(ResourceLocation uid) {
		JadeUIInternal.uid = uid;
	}
}
