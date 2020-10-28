package mcp.mobius.waila.api;

import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * Read-only interface for Waila internal config storage.<br>
 * An instance of this interface is passed to most of Waila callbacks as a way to change the behavior depending on client settings.
 *
 * @author ProfMobius
 */
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

    /**
     * @see #get(ResourceLocation, boolean)
     */
    default boolean get(ResourceLocation key) {
        return get(key, false);
    }

    /**
     * Gets a value from the config with the provided default returned if the key is not registered.
     *
     * @param key The config key
     * @param defaultValue The default value
     * @return The value returned from the config or the default value if none exist.
     */
    boolean get(ResourceLocation key, boolean defaultValue);
}
