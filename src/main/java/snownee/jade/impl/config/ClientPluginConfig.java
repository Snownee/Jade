package snownee.jade.impl.config;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.entry.ConfigEntry;

public class ClientPluginConfig implements IPluginConfig {

	public static final Codec<ClientPluginConfig> CODEC = ServerPluginConfig.DATA_CODEC.xmap(
			ClientPluginConfig::new,
			IPluginConfig::values);

	private final Map<Identifier, Object> values;
	private final Map<Identifier, Object> mergedValues = Maps.newHashMap();

	private ClientPluginConfig(Map<Identifier, Object> values) {
		this.values = values;
		mergedValues.putAll(values);
	}

	@Override
	public boolean get(Identifier key) {
		return (Boolean) Objects.requireNonNull(mergedValues.get(key));
	}

	@Override
	public <T extends Enum<T>> T getEnum(Identifier key) {
		//noinspection unchecked
		return (T) Objects.requireNonNull(mergedValues.get(key));
	}

	@Override
	public int getInt(Identifier key) {
		return ((Number) mergedValues.get(key)).intValue();
	}

	@Override
	public float getFloat(Identifier key) {
		return ((Number) mergedValues.get(key)).floatValue();
	}

	@Override
	public String getString(Identifier key) {
		return (String) Objects.requireNonNull(mergedValues.get(key));
	}

	@Override
	public boolean set(Identifier key, Object value) {
		Objects.requireNonNull(value);
		ConfigEntry<?> entry = WailaClientRegistration.instance().getConfigEntry(key);
		if (entry == null) {
			Jade.LOGGER.warn("Skip setting value for unknown option: {}, {}", key, value);
			return false;
		}
		try {
			value = entry.convertValue(value);
		} catch (Exception e) {
			Jade.LOGGER.warn("Skip setting illegal value for option: {}, {}", key, value);
			return false;
		}
		if (!entry.isValidValue(value)) {
			Jade.LOGGER.warn("Skip setting illegal value for option: {}, {}", key, value);
			return false;
		}
		Object old = values.put(key, value);
		if (!Objects.equals(old, value)) {
			ensureEntry(entry);
		}
		return true;
	}

	@Override
	public Map<Identifier, Object> values() {
		return values;
	}

	public void ensureEntry(ConfigEntry<?> entry) {
		Identifier key = entry.id();
		Object value = values.get(key);
		if (value == null) {
			values.put(key, value = entry.defaultValue());
		} else {
			try {
				values.put(key, value = entry.convertValue(value));
			} catch (Exception e) {
				values.put(key, value = entry.defaultValue());
			}
		}
		if (entry.isSynced()) {
			value = entry.syncedValue();
		}
		Object old = mergedValues.put(key, value);
		if (!Objects.equals(old, value)) {
			entry.notifyChange();
		}
	}

}
