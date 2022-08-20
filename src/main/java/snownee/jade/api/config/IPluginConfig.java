package snownee.jade.api.config;

import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IToggleableProvider;

/**
 * Read-only interface for Waila internal config storage.<br>
 * An instance of this interface is passed to most of Waila callbacks as a way to change the behavior depending on client settings.
 *
 * @author ProfMobius
 */
@NonExtendable
public interface IPluginConfig {

	/**
	 * Gets a collection of all the keys for a given namespace.
	 *
	 * @param namespace The namespace to get keys from
	 * @return all the keys for a given namespace.
	 */
	Set<ResourceLocation> getKeys(String namespace);

	/**
	 * Gets a collection of all keys.
	 *
	 * @return all registered keys.
	 */
	Set<ResourceLocation> getKeys();

	boolean get(IToggleableProvider provider);

	/**
	 * @see #get(ResourceLocation, boolean)
	 */
	default boolean get(ResourceLocation key) {
		return get(key, false);
	}

	/**
	 * Gets a value from the config with the provided default returned if the key is not registered.
	 *
	 * @param key          The config key
	 * @param defaultValue The default value
	 * @return The value returned from the config or the default value if none exist.
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "1.20")
	boolean get(ResourceLocation key, boolean defaultValue);

	<T extends Enum<T>> T getEnum(ResourceLocation key);

	int getInt(ResourceLocation key);

	float getFloat(ResourceLocation key);

	String getString(ResourceLocation key);

	IWailaConfig getWailaConfig();
}
