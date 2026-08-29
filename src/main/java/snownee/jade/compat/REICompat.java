/*
package snownee.jade.compat;

import org.jspecify.annotations.Nullable;

import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class REICompat implements RecipeLookupPlugin {
	@SuppressWarnings("UnstableApiUsage")
	@Override
	public RecipeLookupResult lookup(ItemStack itemStack, @Nullable Identifier specialId, boolean uses) {
		ViewSearchBuilder builder = ViewSearchBuilder.builder();
		if (uses) {
			builder.addUsagesFor(EntryStacks.of(itemStack));
		} else {
			builder.addRecipesFor(EntryStacks.of(itemStack));
		}
		if (builder.streamDisplays().toList().isEmpty()) {
			return RecipeLookupResult.FAIL;
		}
		return new RecipeLookupResult(
				"roughlyenoughitems", 1f, (screen, results) -> {
			if (screen == null) {
				// https://github.com/shedaniel/RoughlyEnoughItems/issues/516
				Minecraft.getInstance().gui.setScreen(new DummyScreen());
			}
			builder.open();
		});
	}

	public static class DummyScreen extends Screen {
		protected DummyScreen() {
			super(CommonComponents.EMPTY);
		}

		@Override
		public void tick() {
			onClose();
		}
	}
}
*/
