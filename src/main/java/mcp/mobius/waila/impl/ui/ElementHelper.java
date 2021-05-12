package mcp.mobius.waila.impl.ui;

import javax.annotation.Nullable;

import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IBorderStyle;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.impl.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class ElementHelper implements IElementHelper {
	public static final ElementHelper INSTANCE = new ElementHelper();

	@Override
	public IElement text(ITextComponent component) {
		return new TextElement(component);
	}

	@Override
	public IElement item(ItemStack stack) {
		return ItemStackElement.of(stack);
	}

	@Override
	public IElement item(ItemStack stack, float scale) {
		return ItemStackElement.of(stack, scale);
	}

	@Override
	public IElement item(ItemStack stack, float scale, String text) {
		return ItemStackElement.of(stack, scale, text);
	}

	@Override
	public IElement fluid(FluidStack fluidStack) {
		return new FluidStackElement(fluidStack);
	}

	@Override
	public IElement spacer(int width, int height) {
		return new SpacerElement(new Vector2f(width, height));
	}

	@Override
	public IElement progress(float progress, @Nullable ITextComponent text, IProgressStyle style, @Nullable IBorderStyle borderStyle) {
		return new ProgressElement(progress, text, (ProgressStyle) style, (BorderStyle) borderStyle);
	}

	@Override
	public IElement box(ITooltip tooltip, @Nullable IBorderStyle border) {
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
