package snownee.jade.impl.config;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.impl.config.entry.EnumConfigEntry;
import snownee.jade.util.JadeCodecs;

public class PluginConfig implements IPluginConfig {

	public static final Codec<PluginConfig> CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, JadeCodecs.PRIMITIVE))
			.xmap($ -> {
				PluginConfig config = new PluginConfig();
				$.forEach((namespace, subMap) -> subMap.forEach((path, value) -> {
					try {
						ResourceLocation key = ResourceLocation.fromNamespaceAndPath(namespace, path);
						config.values.put(key, value);
					} catch (Exception ignored) {
					}
				}));
				return config;
			}, $ -> {
				Map<String, Map<String, Object>> map = Maps.newHashMap();
				$.values.forEach((key, value) -> {
					String namespace = key.getNamespace();
					String path = key.getPath();
					Map<String, Object> subMap = map.computeIfAbsent(namespace, k -> Maps.newHashMap());
					//noinspection rawtypes
					if (value instanceof Enum e) {
						value = e.name();
					}
					subMap.put(path, value);
				});
				return map;
			});

	private final Map<ResourceLocation, Object> values = Maps.newHashMap();
	private JsonObject serverConfigs;

	private PluginConfig() {
	}

	public static boolean isPrimaryKey(ResourceLocation key) {
		return !key.getPath().contains(".");
	}

	public static ResourceLocation getPrimaryKey(ResourceLocation key) {
		return key.withPath(key.getPath().substring(0, key.getPath().indexOf('.')));
	}

	@Override
	public boolean get(IToggleableProvider provider) {
		if (provider.isRequired()) {
			return true;
		}
		return get(provider.getUid());
	}

	@Override
	public boolean get(ResourceLocation key) {
		return (Boolean) Objects.requireNonNull(values.get(key));
	}

	@Override
	public <T extends Enum<T>> T getEnum(ResourceLocation key) {
		//noinspection unchecked
		return (T) Objects.requireNonNull(values.get(key));
	}

	@Override
	public int getInt(ResourceLocation key) {
		return ((Number) values.get(key)).intValue();
	}

	@Override
	public float getFloat(ResourceLocation key) {
		return ((Number) values.get(key)).floatValue();
	}

	@Override
	public String getString(ResourceLocation key) {
		return (String) Objects.requireNonNull(values.get(key));
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
			entry.notifyChange();
		}
		return true;
	}

//	public void applyServerConfigs(JsonObject json) {
//		json.keySet().forEach(namespace -> {
//			json.getAsJsonObject(namespace).entrySet().forEach(entry -> {
//				ResourceLocation key = ResourceLocation.fromNamespaceAndPath(namespace, entry.getKey());
//				ConfigEntry<?> configEntry = getEntry(key);
//				if (configEntry != null) {
//					JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
//					Object v;
//					if (primitive.isBoolean()) {
//						v = primitive.getAsBoolean();
//					} else if (primitive.isNumber()) {
//						v = primitive.getAsNumber();
//					} else if (primitive.isString()) {
//						v = primitive.getAsString();
//					} else {
//						return;
//					}
//					if (configEntry.isValidValue(v)) {
//						configEntry.convertValue(v);
//						configEntry.setSynced(true);
//					}
//				}
//			});
//		});
//	}

	public String getServerConfigs() {
		return serverConfigs == null ? "" : serverConfigs.toString();
	}

	public void ensureEntry(ConfigEntry<?> entry) {
		ResourceLocation key = entry.getId();
		if (!values.containsKey(key)) {
			values.put(key, entry.getDefaultValue());
		} else if (entry instanceof EnumConfigEntry<?> enumEntry && values.get(key) instanceof String s) {
			try {
				values.put(key, enumEntry.convertValue(s));
			} catch (Exception e) {
				values.put(key, entry.getDefaultValue());
			}
		}
	}

//	public void addConfigListener(ResourceLocation key, Consumer<ResourceLocation> listener) {
//		Preconditions.checkArgument(containsKey(key));
//		configs.get(key).addListener(listener);
//	}

}
