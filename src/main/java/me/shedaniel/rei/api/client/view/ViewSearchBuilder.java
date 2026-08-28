package me.shedaniel.rei.api.client.view;

import java.util.Set;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;

public interface ViewSearchBuilder {
	static ViewSearchBuilder builder() {
		throw new UnsupportedOperationException();
	}

	<T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack);

	<T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack);

	Set<CategoryIdentifier<?>> getCategories();

	default boolean open() {
		return false;
	}
}
