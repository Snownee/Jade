package mcp.mobius.waila.api.ui;

import javax.annotation.Nullable;

import mcp.mobius.waila.api.ITooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public interface IElementHelper {

	IElement text(ITextComponent component);

	IElement spacer(int x, int y);

	default IElement item(ItemStack stack) {
		return item(stack, 1);
	}

	IElement item(ItemStack itemStack, float scale);

	IElement fluid(FluidStack fluidStack);

	IElement progress(float progress, @Nullable ITextComponent text, IProgressStyle style, @Nullable IBorderStyle borderStyle);

	default IElement box(ITooltip tooltip) {
		return box(tooltip, borderStyle());
	}

	IElement box(ITooltip tooltip, @Nullable IBorderStyle border);

	ITooltip tooltip();

	IBorderStyle borderStyle();

	IProgressStyle progressStyle();
}
