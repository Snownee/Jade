package mcp.mobius.waila.api.ui;

import javax.annotation.Nullable;

import mcp.mobius.waila.api.ITooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IElementHelper {

	IElement text(Component component);

	IElement spacer(int x, int y);

	IElement item(ItemStack itemStack);

	IElement item(ItemStack itemStack, float scale);

	IElement item(ItemStack itemStack, float scale, @Nullable String text);

	IElement fluid(FluidStack fluidStack);

	IElement progress(float progress, @Nullable Component text, IProgressStyle style, @Nullable IBorderStyle borderStyle);

	default IElement box(ITooltip tooltip) {
		return box(tooltip, borderStyle());
	}

	/**
	 * Display a nested tooltip
	 */
	IElement box(ITooltip tooltip, @Nullable IBorderStyle border);

	/**
	 * Create an empty tooltip. Used by the {@code box} method.
	 */
	ITooltip tooltip();

	IBorderStyle borderStyle();

	IProgressStyle progressStyle();
}
