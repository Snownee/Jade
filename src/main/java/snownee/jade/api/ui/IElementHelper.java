package snownee.jade.api.ui;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import snownee.jade.api.ITooltip;
import snownee.jade.impl.ui.ElementHelper;

public interface IElementHelper {

	static IElementHelper get() {
		return ElementHelper.INSTANCE;
	}

	IElement text(Component component);

	IElement spacer(int x, int y);

	IElement item(ItemStack itemStack);

	IElement item(ItemStack itemStack, float scale);

	IElement item(ItemStack itemStack, float scale, @Nullable String text);

	IElement fluid(FluidState fluidState);

	@ScheduledForRemoval(inVersion = "1.20")
	IElement progress(float progress, @Nullable Component text, IProgressStyle style, @Nullable IBorderStyle borderStyle);

	IElement progress(float progress, @Nullable Component text, IProgressStyle style, IBoxStyle boxStyle, boolean canDecrease);

	@ScheduledForRemoval(inVersion = "1.20")
	default IElement box(ITooltip tooltip) {
		return box(tooltip, BoxStyle.DEFAULT);
	}

	@ScheduledForRemoval(inVersion = "1.20")
	IElement box(ITooltip tooltip, @Nullable IBorderStyle border);

	/**
	 * Display a nested tooltip
	 */
	IBoxElement box(ITooltip tooltip, IBoxStyle boxStyle);

	/**
	 * Create an empty tooltip. Used by the {@code box} method.
	 */
	ITooltip tooltip();

	@ScheduledForRemoval(inVersion = "1.20")
	IBorderStyle borderStyle();

	IProgressStyle progressStyle();

}
