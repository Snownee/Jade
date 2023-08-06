package snownee.jade.test;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

public enum ExampleItemStorageProvider implements IServerExtensionProvider<BrewingStandBlockEntity, ItemStack>,
		IClientExtensionProvider<ItemStack, ItemView> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_BREWING;
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
		return ClientViewGroup.map(groups, ItemView::new, (group, clientGroup) -> {
			clientGroup.title = Component.literal(group.id);
			clientGroup.bgColor = 0x55666666;
		});
	}

	@Override
	public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor, BrewingStandBlockEntity target) {
		var potions = new ViewGroup<>(IntStream.of(0, 1, 2).mapToObj(target::getItem).filter($ -> !$.isEmpty()).toList());
		potions.id = "Potions";
		var ingredient = new ViewGroup<>(IntStream.of(3).mapToObj(target::getItem).filter($ -> !$.isEmpty()).toList());
		ingredient.id = "Ingredient";
		return List.of(ingredient, potions);
	}
}
