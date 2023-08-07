package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.jade.Internals;
import snownee.jade.api.ITooltip;
import snownee.jade.api.fluid.JadeFluidObject;

public interface IElementHelper {

	static IElementHelper get() {
		return Internals.getElementHelper();
	}

	IElement text(Component component);

	IElement spacer(int x, int y);

	IElement item(ItemStack itemStack);

	IElement item(ItemStack itemStack, float scale);

	IElement item(ItemStack itemStack, float scale, @Nullable String text);

	IElement smallItem(ItemStack itemStack);

	IElement fluid(JadeFluidObject fluid);

	IElement progress(float progress, @Nullable Component text, IProgressStyle style, BoxStyle boxStyle, boolean canDecrease);

	/**
	 * Display a nested tooltip
	 */
	IBoxElement box(ITooltip tooltip, BoxStyle boxStyle);

	/**
	 * Create an empty tooltip. Used by the {@code box} method.
	 */
	ITooltip tooltip();

	IProgressStyle progressStyle();

}
