package me.shedaniel.rei.api.common.util;

import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.world.item.ItemStack;

public final class EntryStacks {
	private EntryStacks() {
	}

	public static EntryStack<ItemStack> of(ItemStack stack) {
		throw new UnsupportedOperationException();
	}
}
