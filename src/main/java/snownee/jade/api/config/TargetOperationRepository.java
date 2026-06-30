package snownee.jade.api.config;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

/**
 * Tracks explicit hide/pick overrides for registry-backed targets.
 *
 * @param <T> registry value type
 * @param <U> resolved runtime object type
 */
public interface TargetOperationRepository<T, U> {
	/**
	 * Reloads repository state from registries.
	 *
	 * @param provider registry lookup provider
	 */
	void reload(HolderLookup.Provider provider);

	/**
	 * Returns whether the given registry entry should be hidden.
	 *
	 * @param key registry key
	 * @return {@code true} if the target should be hidden
	 */
	boolean shouldHide(ResourceKey<T> key);

	/**
	 * Returns whether the given object should be hidden.
	 *
	 * @param obj target object
	 * @return {@code true} if the target should be hidden
	 */
	default boolean shouldHide(U obj) {
		return shouldHide(map(obj));
	}

	/**
	 * Returns whether the given registry entry should be selected.
	 *
	 * @param key registry key
	 * @return {@code true} if the target should be picked
	 */
	boolean shouldPick(ResourceKey<T> key);

	/**
	 * Returns whether the given object should be selected.
	 *
	 * @param obj target object
	 * @return {@code true} if the target should be picked
	 */
	default boolean shouldPick(U obj) {
		return shouldPick(map(obj));
	}

	/**
	 * Marks a registry entry as hidden.
	 *
	 * @param key registry key
	 */
	void hide(ResourceKey<T> key);

	/**
	 * Marks a registry entry as preferred.
	 *
	 * @param key registry key
	 */
	void pick(ResourceKey<T> key);

	/**
	 * Maps a runtime object back to its registry key.
	 *
	 * @param obj runtime object
	 * @return the corresponding registry key
	 */
	ResourceKey<T> map(U obj);
}
