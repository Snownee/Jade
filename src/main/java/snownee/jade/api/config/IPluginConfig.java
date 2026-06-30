package snownee.jade.api.config;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.resources.Identifier;
import snownee.jade.api.IToggleableProvider;

/**
 * Read-only view over the active plugin configuration.
 * <p>
 * Jade passes this object to client-side callbacks so providers can adapt their output to user settings.
 */
@NonExtendable
public interface IPluginConfig {

	/**
	 * Returns whether a key is a top-level configuration entry.
	 *
	 * @param key configuration key
	 * @return {@code true} if the key has no secondary path segment
	 */
	static boolean isPrimaryKey(Identifier key) {
		return !key.getPath().contains(".");
	}

	/**
	 * Returns the top-level portion of a nested configuration key.
	 *
	 * @param key configuration key
	 * @return the primary key
	 */
	static Identifier getPrimaryKey(Identifier key) {
		return key.withPath(key.getPath().substring(0, key.getPath().indexOf('.')));
	}

	/**
	 * Returns the value associated with a toggleable provider.
	 *
	 * @param provider provider to query
	 * @return {@code true} if the provider is enabled
	 */
	default boolean get(IToggleableProvider provider) {
		if (provider.isRequired()) {
			return true;
		}
		return get(provider.getUid());
	}

	/**
	 * Returns a boolean configuration value.
	 *
	 * @param key configuration key
	 * @return the stored value
	 */
	boolean get(Identifier key);

	/**
	 * Returns an enum configuration value.
	 *
	 * @param key configuration key
	 * @param <T> enum type
	 * @return the stored enum value
	 */
	<T extends Enum<T>> T getEnum(Identifier key);

	/**
	 * Returns an integer configuration value.
	 *
	 * @param key configuration key
	 * @return the stored value
	 */
	int getInt(Identifier key);

	/**
	 * Returns a floating-point configuration value.
	 *
	 * @param key configuration key
	 * @return the stored value
	 */
	float getFloat(Identifier key);

	/**
	 * Returns a string configuration value.
	 *
	 * @param key configuration key
	 * @return the stored value
	 */
	String getString(Identifier key);

	/**
	 * Updates a configuration entry.
	 *
	 * @param key configuration key
	 * @param value new value
	 * @return {@code true} if the value was accepted
	 */
	boolean set(Identifier key, Object value);

	/**
	 * Returns all stored configuration values.
	 *
	 * @return immutable or live values map
	 */
	Map<Identifier, Object> values();
}
