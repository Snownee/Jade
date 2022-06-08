package snownee.jade.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.world.item.ItemStack;
import snownee.jade.JadeClient;
import snownee.jade.api.Accessor;
import snownee.jade.impl.ObjectDataCenter;

public class REICompat implements REIClientPlugin {

	public static void onKeyPressed(int action) {
		if (JadeClient.showRecipes == null || JadeClient.showUses == null)
			return;
		if (action != 1)
			return;
		if (!JadeClient.showRecipes.consumeClick() && !JadeClient.showUses.consumeClick())
			return;
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null)
			return;
		ItemStack stack = accessor.getPickedResult();
		if (stack.isEmpty())
			return;

		if (JadeClient.showUses.isDown()) {
			ViewSearchBuilder.builder().addUsagesFor(EntryStack.of(VanillaEntryTypes.ITEM, stack)).open();
		} else {
			ViewSearchBuilder.builder().addRecipesFor(EntryStack.of(VanillaEntryTypes.ITEM, stack)).open();
		}
	}
}