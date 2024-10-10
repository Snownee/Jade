package snownee.jade.api.config;

import java.util.Map;

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

	static boolean isPrimaryKey(ResourceLocation key) {
		return !key.getPath().contains(".");
	}

	static ResourceLocation getPrimaryKey(ResourceLocation key) {
		return key.withPath(key.getPath().substring(0, key.getPath().indexOf('.')));
	}

	default boolean get(IToggleableProvider provider) {
		if (provider.isRequired()) {
			return true;
		}
		return get(provider.getUid());
	}

	boolean get(ResourceLocation key);

	<T extends Enum<T>> T getEnum(ResourceLocation key);

	int getInt(ResourceLocation key);

	float getFloat(ResourceLocation key);

	String getString(ResourceLocation key);

	boolean set(ResourceLocation key, Object value);

	Map<ResourceLocation, Object> values();
}
