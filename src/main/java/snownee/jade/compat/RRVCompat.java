package snownee.jade.compat;

import cc.cassian.rrv.api.ActionType;
import cc.cassian.rrv.common.overlay.itemlist.view.ItemViewOverlay;
import org.jspecify.annotations.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.JadeIds;

public class RRVCompat implements RecipeLookupPlugin {

	public static final Identifier ID = JadeIds.JADE("main");

	@Override
	public RecipeLookupResult lookup(ItemStack itemStack, @Nullable Identifier specialId, boolean uses) {
		return new RecipeLookupResult(
				"rrv", 0.9f, (s, results) -> ItemViewOverlay.INSTANCE.openRecipeView(itemStack, uses ? ActionType.INPUT : ActionType.RESULT));
	}
}
