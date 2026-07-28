package cc.cassian.rrv.common.overlay.itemlist.view;

import cc.cassian.rrv.api.ActionType;
import net.minecraft.world.item.ItemStack;

public class ItemViewOverlay {
	public static final ItemViewOverlay INSTANCE = new ItemViewOverlay();

	public void openRecipeView(ItemStack itemStack, ActionType actionType) {
		throw new AssertionError();
	}
}
