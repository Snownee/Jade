package snownee.jade.compat;

import java.util.List;

import org.jspecify.annotations.Nullable;

import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
			builder.addUsagesFor(EntryStack.of(VanillaEntryTypes.ITEM, itemStack));
		} else {
			builder.addRecipesFor(EntryStack.of(VanillaEntryTypes.ITEM, itemStack));
		}
		List<DisplaySpec> list = builder.streamDisplays().toList();
		if (list.isEmpty()) {
			return RecipeLookupResult.FAIL;
		}
		return new RecipeLookupResult(
				"roughlyenoughitems", 1f, (screen, results) -> {
			if (screen == null) {
				// https://github.com/shedaniel/RoughlyEnoughItems/issues/516
				Minecraft.getInstance().setScreen(new DummyScreen());
				Minecraft.getInstance().setScreen(null);
			}
			builder.open();
		});
	}

	public static class DummyScreen extends Screen {
		protected DummyScreen() {
			super(CommonComponents.EMPTY);
		}

		@Override
		protected void renderBlurredBackground(GuiGraphics guiGraphics) {
			// NO-OP
		}

		@Override
		public void tick() {
			onClose();
		}
	}
}
