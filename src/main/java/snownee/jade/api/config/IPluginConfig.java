package snownee.jade.api.config;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.resources.Identifier;
import snownee.jade.api.IToggleableProvider;

/**
 * Read-only interface for Waila internal config storage.<br>
 * An instance of this interface is passed to most of Waila callbacks as a way to change the behavior depending on client settings.
 *
 * @author ProfMobius
 */
@NonExtendable
public interface IPluginConfig {

	static boolean isPrimaryKey(Identifier key) {
		return !key.getPath().contains(".");
	}

	static Identifier getPrimaryKey(Identifier key) {
		return key.withPath(key.getPath().substring(0, key.getPath().indexOf('.')));
	}

	default boolean get(IToggleableProvider provider) {
		if (provider.isRequired()) {
			return true;
		}
		return get(provider.getUid());
	}

	boolean get(Identifier key);

	<T extends Enum<T>> T getEnum(Identifier key);

	int getInt(Identifier key);

	float getFloat(Identifier key);

	String getString(Identifier key);

	boolean set(Identifier key, Object value);

	Map<Identifier, Object> values();
}
