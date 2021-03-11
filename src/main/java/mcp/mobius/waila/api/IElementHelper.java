package mcp.mobius.waila.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IElementHelper {

    IElement text(ITextComponent component);

    IElement spacer(int x, int y);

    default IElement item(ItemStack stack) {
        return item(stack, 1);
    }

    IElement item(ItemStack stack, float scale);

    IElement progress(float progress);

    IElement box(ITooltip tooltip); //TODO border

    ITooltip createTooltip();

}
