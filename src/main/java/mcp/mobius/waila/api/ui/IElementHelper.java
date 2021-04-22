package mcp.mobius.waila.api.ui;

import javax.annotation.Nullable;

import mcp.mobius.waila.api.ITooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IElementHelper {

	IElement text(ITextComponent component);

	IElement spacer(int x, int y);

	default IElement item(ItemStack stack) {
		return item(stack, 1);
	}

	IElement item(ItemStack stack, float scale);

	IElement progress(float progress, @Nullable ITextComponent text, IProgressStyle style, IBorderStyle borderStyle);

	default IElement box(ITooltip tooltip) {
		return box(tooltip, borderStyle());
	}

	IElement box(ITooltip tooltip, IBorderStyle border);

	ITooltip tooltip();

	IBorderStyle borderStyle();

	IProgressStyle progressStyle();
}
