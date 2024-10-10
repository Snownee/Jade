package snownee.jade.impl.config;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.JadeCodecs;
import snownee.jade.util.JsonConfig;

public class ServerPluginConfig implements IPluginConfig {
	public static final String FILE = Jade.ID + "/server-plugin-overrides.json";

	public static final Codec<Map<ResourceLocation, Object>> DATA_CODEC = Codec.unboundedMap(
			Codec.STRING,
			Codec.unboundedMap(Codec.STRING, JadeCodecs.PRIMITIVE)).xmap($ -> {
		Map<ResourceLocation, Object> map = Maps.newHashMap();
		$.forEach((namespace, subMap) -> subMap.forEach((path, value) -> {
			try {
				ResourceLocation key = ResourceLocation.fromNamespaceAndPath(namespace, path);
				map.put(key, value);
			} catch (Exception ignored) {
			}
		}));
		return map;
	}, $ -> {
		Map<String, Map<String, Object>> map = Maps.newHashMap();
		$.forEach((key, value) -> {
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

	public static final Codec<ServerPluginConfig> CODEC = DATA_CODEC.xmap(ServerPluginConfig::new, IPluginConfig::values);
	private static final JsonConfig<ServerPluginConfig> INSTANCE = new JsonConfig<>(FILE, CODEC, null);

	public static ServerPluginConfig instance() {
		return INSTANCE.get();
	}

	private final Map<ResourceLocation, Object> values;

	private ServerPluginConfig(Map<ResourceLocation, Object> values) {
		this.values = values;
	}

	@Override
	public boolean get(ResourceLocation key) {
		return (Boolean) Objects.requireNonNull(values.get(key));
	}

	@Override
	public <T extends Enum<T>> T getEnum(ResourceLocation key) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<ResourceLocation, Object> values() {
		return values;
	}
}
