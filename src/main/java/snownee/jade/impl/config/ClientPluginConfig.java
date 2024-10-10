package snownee.jade.impl.config;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.impl.config.entry.EnumConfigEntry;

public class ClientPluginConfig implements IPluginConfig {

	public static final Codec<ClientPluginConfig> CODEC = ServerPluginConfig.DATA_CODEC.xmap(
			ClientPluginConfig::new,
			IPluginConfig::values);

	private final Map<ResourceLocation, Object> values;
	private final Map<ResourceLocation, Object> mergedValues = Maps.newHashMap();

	private ClientPluginConfig(Map<ResourceLocation, Object> values) {
		this.values = values;
		mergedValues.putAll(values);
	}

	@Override
	public boolean get(ResourceLocation key) {
		return (Boolean) Objects.requireNonNull(mergedValues.get(key));
	}

	@Override
	public <T extends Enum<T>> T getEnum(ResourceLocation key) {
		//noinspection unchecked
		return (T) Objects.requireNonNull(mergedValues.get(key));
	}

	@Override
	public int getInt(ResourceLocation key) {
		return ((Number) mergedValues.get(key)).intValue();
	}

	@Override
	public float getFloat(ResourceLocation key) {
		return ((Number) mergedValues.get(key)).floatValue();
	}

	@Override
	public String getString(ResourceLocation key) {
		return (String) Objects.requireNonNull(mergedValues.get(key));
	}

	@Override
	public boolean set(ResourceLocation key, Object value) {
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
	public Map<ResourceLocation, Object> values() {
		return values;
	}

	public void ensureEntry(ConfigEntry<?> entry) {
		ResourceLocation key = entry.id();
		Object value;
		if (!values.containsKey(key)) {
			values.put(key, value = entry.defaultValue());
		} else if (entry instanceof EnumConfigEntry<?> enumEntry && values.get(key) instanceof String s) {
			try {
				values.put(key, value = enumEntry.convertValue(s));
			} catch (Exception e) {
				values.put(key, value = entry.defaultValue());
			}
		} else {
			value = values.get(key);
		}
		value = entry.isSynced() ? entry.syncedValue() : value;
		Object old = mergedValues.put(key, value);
		if (!Objects.equals(old, value)) {
			entry.notifyChange();
		}
	}

}
