package snownee.jade.api.config;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

public interface TargetOperationRepository<T, U> {
	void reload(HolderLookup.Provider provider);

	boolean shouldHide(ResourceKey<T> key);

	default boolean shouldHide(U obj) {
		return shouldHide(map(obj));
	}

	boolean shouldPick(ResourceKey<T> key);

	default boolean shouldPick(U obj) {
		return shouldPick(map(obj));
	}

	void hide(ResourceKey<T> key);

	void pick(ResourceKey<T> key);

	ResourceKey<T> map(U obj);
}
