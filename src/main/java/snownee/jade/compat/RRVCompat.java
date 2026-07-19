package snownee.jade.compat;

import org.jspecify.annotations.Nullable;

import cc.cassian.rrv.api.ActionType;
import cc.cassian.rrv.common.overlay.itemlist.view.ItemViewOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class RRVCompat implements RecipeLookupPlugin {
	@Override
	public RecipeLookupResult lookup(ItemStack itemStack, @Nullable Identifier specialId, boolean uses) {
		return new RecipeLookupResult(
				"rrv", 0.9f, (_, _) -> ItemViewOverlay.INSTANCE.openRecipeView(itemStack, uses ? ActionType.INPUT : ActionType.RESULT));
	}
}
