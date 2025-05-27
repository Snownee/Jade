package snownee.jade.impl.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.ui.TextElement;
import snownee.jade.impl.Tooltip;

public class ElementHelper implements IElementHelper {
	public static final ElementHelper INSTANCE = new ElementHelper();
	public static final ResourceLocation DEFAULT_PROGRESS = JadeIds.JADE("progress");
	public static final ResourceLocation DEFAULT_PROGRESS_BASE = JadeIds.JADE("progress_base");
	public static final Vec2 SMALL_ITEM_SIZE = new Vec2(10, 9);
	public static final Vec2 SMALL_ITEM_OFFSET = new Vec2(0, -1); //Vec2.NEG_UNIT_Y nullified by Saturn mod
	private ResourceLocation uid;

	@Override
	public TextElement text(Component component) {
		return new TextElementImpl(component);
	}

	@Override
	public Element item(ItemStack stack) {
		return ItemStackElement.of(stack);
	}

	@Override
	public Element item(ItemStack stack, float scale) {
		return ItemStackElement.of(stack, scale);
	}

	@Override
	public Element item(ItemStack stack, float scale, String text) {
		return ItemStackElement.of(stack, scale, text);
	}

	@Override
	public Element smallItem(ItemStack stack) {
		return item(stack, 0.5F, "").narration("");
	}

	@Override
	public Element fluid(JadeFluidObject fluid) {
		return new FluidStackElement(fluid);
	}

	@Override
	public Element spacer(int width, int height) {
		return new SpacerElement(width, height);
	}

	@Override
	public Element progress(float progress, @Nullable Component text, ProgressStyle style, BoxStyle boxStyle, boolean canDecrease) {
		Objects.requireNonNull(style);
		Objects.requireNonNull(boxStyle);
		return new ProgressElement(progress, text, style, boxStyle, canDecrease);
	}

	@Override
	public Element progress(float progress) {
		return progress(progress, DEFAULT_PROGRESS_BASE, DEFAULT_PROGRESS, 22, 16, false);
	}

	@Override
	public Element progress(
			float progress,
			ResourceLocation baseSprite,
			ResourceLocation progressSprite,
			int width,
			int height,
			boolean canDecrease) {
//		ProgressStyle style = progressStyle().fitContentX(false).overlay(sprite(progressSprite, width, height));
//		BoxStyle boxStyle = BoxStyle.getSprite(baseSprite, null);
//		return progress(progress, null, style, boxStyle, canDecrease).size(new Vec2(width, height));
		return spacer(10, 10);
	}

	@Override
	public BoxElement box(ITooltip tooltip, BoxStyle boxStyle) {
		return new BoxElementImpl((Tooltip) tooltip, boxStyle);
	}

	@Override
	public ITooltip tooltip() {
		return new Tooltip();
	}

	@Override
	public ProgressStyle progressStyle() {
		return new SimpleProgressStyle();
	}

	@Override
	public Element sprite(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height) {
		return new SpriteElement(renderPipeline, sprite, width, height);
	}

	@Override
	public Element sprite(ResourceLocation sprite, int width, int height) {
		return new SpriteElement(sprite, width, height);
	}

	@Nullable
	public ResourceLocation currentUid() {
		return uid;
	}

	public void setCurrentUid(ResourceLocation uid) {
		this.uid = uid;
	}

}
