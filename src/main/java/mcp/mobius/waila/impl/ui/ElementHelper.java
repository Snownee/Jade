package mcp.mobius.waila.impl.ui;

import javax.annotation.Nullable;

import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IBorderStyle;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.api.ui.Size;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.overlay.element.BoxElement;
import mcp.mobius.waila.overlay.element.ItemStackElement;
import mcp.mobius.waila.overlay.element.ProgressElement;
import mcp.mobius.waila.overlay.element.SpacerElement;
import mcp.mobius.waila.overlay.element.TextElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import snownee.jade.Jade;

public class ElementHelper implements IElementHelper {
	public static final ElementHelper INSTANCE = new ElementHelper();

	static final ResourceLocation ITEM = new ResourceLocation("item");
	static final ResourceLocation SPACER = new ResourceLocation("spacer");

	static final ResourceLocation OFFSET_TEXT = new ResourceLocation(Jade.MODID, "text");
	static final ResourceLocation BORDER = new ResourceLocation(Jade.MODID, "border");
	static final ResourceLocation SUB = new ResourceLocation(Jade.MODID, "sub");

	@Override
	public IElement text(ITextComponent component) {
		return new TextElement(component);
	}

	@Override
	public IElement item(ItemStack stack, float scale) {
		return ItemStackElement.of(stack, scale);
	}

	@Override
	public IElement spacer(int width, int height) {
		return new SpacerElement(new Size(width, height));
	}

	@Override
	public IElement progress(float progress, @Nullable ITextComponent text, IProgressStyle style, IBorderStyle borderStyle) {
		return new ProgressElement(progress, text, (ProgressStyle) style, (BorderStyle) borderStyle);
	}

	@Override
	public IElement box(ITooltip tooltip, IBorderStyle border) {
		return new BoxElement((Tooltip) tooltip, (BorderStyle) border);
	}

	@Override
	public ITooltip tooltip() {
		return new Tooltip();
	}

	@Override
	public IBorderStyle borderStyle() {
		return new BorderStyle();
	}

	@Override
	public IProgressStyle progressStyle() {
		return new ProgressStyle();
	}

	//
	//    public static IElement sub(String text) {
	//        CompoundNBT tag = new CompoundNBT();
	//        tag.putString("text", text);
	//        return new RenderableTextComponent(SUB, tag);
	//    }

}
