package snownee.jade.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemStacks {

	private ItemStacks() {
	}

	/**
	 * Creates an {@link ItemStack} that is safe to render even when no level is loaded.
	 * <p>
	 * Item components are bound to their holders only during datapack loading or registry sync, i.e. when a
	 * level is loaded or a server is joined. Before that, {@code item.builtInRegistryHolder().components()}
	 * throws, making {@code new ItemStack(item)} unusable (for example on the title screen). This replaces the
	 * registry-backed holder with a {@link Holder.Direct} that supplies the components itself.
	 * <p>
	 * The result only carries the item model, so state-dependent visuals (enchantment glint, potion color,
	 * banner patterns, ...) fall back to their defaults.
	 */
	public static ItemStack of(Item item) {
		return of(item, 1);
	}

	public static ItemStack of(Item item, int count) {
		if (item == Items.AIR) {
			return ItemStack.EMPTY;
		}
		Holder.Reference<Item> holder = item.builtInRegistryHolder();
		if (holder.areComponentsBound()) {
			return new ItemStack(holder, count);
		}
		Identifier model = BuiltInRegistries.ITEM.getKey(item);
		DataComponentMap components = DataComponentMap.builder()
				.set(DataComponents.ITEM_MODEL, model)
				.build();
		return new ItemStack(new Holder.Direct<>(item, components), count);
	}
}
