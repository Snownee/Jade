package snownee.jade.impl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.PlatformProxy;

public class PluginConfig implements IPluginConfig {

	public static final PluginConfig INSTANCE = new PluginConfig();
	public static final String CLIENT_FILE = Jade.MODID + "/plugins.json";
	public static final String SERVER_FILE = Jade.MODID + "/server-plugin-overrides.json";

	private final Map<ResourceLocation, ConfigEntry<Object>> configs;
	private JsonObject serverConfigs;

	private PluginConfig() {
		configs = Maps.newHashMap();
	}

	public void addConfig(ConfigEntry<?> entry) {
		Preconditions.checkArgument(StringUtils.countMatches(entry.getId().getPath(), '.') <= 1);
		Preconditions.checkArgument(!containsKey(entry.getId()), "Duplicate config key: {}", entry.getId());
		configs.put(entry.getId(), (ConfigEntry<Object>) entry);
	}

	@Override
	public Set<ResourceLocation> getKeys(String namespace) {
		return getKeys().stream().filter(id -> id.getNamespace().equals(namespace)).collect(Collectors.toSet());
	}

	@Override
	public Set<ResourceLocation> getKeys() {
		return configs.keySet();
	}

	@Override
	public boolean get(IToggleableProvider provider) {
		if (provider.isRequired()) {
			return true;
		}
		return get(provider.getUid(), provider.enabledByDefault());
	}

	@Override
	public boolean get(ResourceLocation key, boolean defaultValue) {
		if (PlatformProxy.isPhysicallyClient()) {
			ConfigEntry<?> entry = getEntry(key);
			return entry == null ? defaultValue : (Boolean) entry.getValue();
		} else {
			return Optional.ofNullable(serverConfigs)
					.map($ -> $.getAsJsonObject(key.getNamespace()))
					.map($ -> $.get(key.getPath()))
					.map(JsonElement::getAsBoolean)
					.orElse(false);
		}
	}

	@Override
	public <T extends Enum<T>> T getEnum(ResourceLocation key) {
		return (T) getEntry(key).getValue();
	}

	@Override
	public int getInt(ResourceLocation key) {
		return (Integer) getEntry(key).getValue();
	}

	@Override
	public float getFloat(ResourceLocation key) {
		return (Float) getEntry(key).getValue();
	}

	@Override
	public String getString(ResourceLocation key) {
		return (String) getEntry(key).getValue();
	}

	public List<String> getNamespaces() {
		return configs.keySet().stream().sorted((o1, o2) -> o1.getNamespace().compareToIgnoreCase(o2.getNamespace())).map(ResourceLocation::getNamespace).distinct().collect(Collectors.toList());
	}

	public ConfigEntry<?> getEntry(ResourceLocation key) {
		return configs.get(key);
	}

	public boolean set(ResourceLocation key, Object value) {
		Objects.requireNonNull(value);
		ConfigEntry<?> entry = getEntry(key);
		if (entry == null) {
			Jade.LOGGER.warn("Skip setting value for unknown option: {}, {}", key, value);
			return false;
		}
		if (!entry.isValidValue(value)) {
			Jade.LOGGER.warn("Skip setting illegal value for option: {}, {}", key, value);
			return false;
		}
		entry.setValue(value);
		return true;
	}

	public void reload() {
		boolean client = PlatformProxy.isPhysicallyClient();
		File configFile = new File(PlatformProxy.getConfigDirectory(), client ? CLIENT_FILE : SERVER_FILE);

		if (client)
			configs.values().forEach($ -> $.setSynced(false));

		if (!configFile.exists()) { // Write defaults, but don't read
			writeConfig(configFile, true);
		} else { // Read back from config
			if (client) {
				Map<String, Map<String, Object>> config;
				try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
					config = JsonConfig.DEFAULT_GSON.fromJson(reader, new TypeToken<Map<String, Map<String, Object>>>() {
					}.getType());
				} catch (Exception e) {
					e.printStackTrace();
					config = Maps.newHashMap();
				}

				MutableBoolean saveFlag = new MutableBoolean();
				Set<ResourceLocation> found = Sets.newHashSet();
				config.forEach((namespace, subMap) -> subMap.forEach((path, value) -> {
					ResourceLocation id = new ResourceLocation(namespace, path);
					if (!configs.containsKey(id)) {
						return;
					}
					if (!set(id, value))
						saveFlag.setTrue();
					found.add(id);
				}));

				Set<ResourceLocation> allKeys = getKeys();
				for (ResourceLocation id : allKeys) {
					if (!found.contains(id)) {
						set(id, getEntry(id).getDefaultValue());
						saveFlag.setTrue();
					}
				}

				if (saveFlag.isTrue())
					save();
			} else {
				try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
					serverConfigs = JsonConfig.DEFAULT_GSON.fromJson(reader, JsonObject.class);
				} catch (Exception e) {
					e.printStackTrace();
					serverConfigs = null;
				}
			}
		}
	}

	public void save() {
		File configFile = new File(PlatformProxy.getConfigDirectory(), CLIENT_FILE);
		writeConfig(configFile, false);
	}

	private void writeConfig(File file, boolean reset) {
		boolean client = PlatformProxy.isPhysicallyClient();
		String json;
		if (client) {
			Map<String, Map<String, Object>> config = Maps.newHashMap();
			configs.values().forEach(e -> {
				Map<String, Object> modConfig = config.computeIfAbsent(e.getId().getNamespace(), k -> Maps.newHashMap());
				if (reset)
					e.setValue(e.getDefaultValue());
				modConfig.put(e.getId().getPath(), e.getValue());
			});
			json = JsonConfig.DEFAULT_GSON.toJson(config);
		} else {
			json = "{}";
		}
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IWailaConfig getWailaConfig() {
		return Jade.CONFIG.get();
	}

	public void applyServerConfigs(JsonObject json) {
		json.keySet().forEach(namespace -> {
			json.getAsJsonObject(namespace).entrySet().forEach(entry -> {
				ResourceLocation key = new ResourceLocation(namespace, entry.getKey());
				ConfigEntry<?> configEntry = getEntry(key);
				if (configEntry != null) {
					JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
					Object v;
					if (primitive.isBoolean())
						v = primitive.getAsBoolean();
					else if (primitive.isNumber())
						v = primitive.getAsNumber();
					else if (primitive.isString())
						v = primitive.getAsString();
					else return;
					if (configEntry.isValidValue(v)) {
						configEntry.setValue(v);
						configEntry.setSynced(true);
					}
				}
			});
		});
	}

	public String getServerConfigs() {
		return serverConfigs == null ? "" : serverConfigs.toString();
	}

	public boolean containsKey(ResourceLocation uid) {
		return configs.containsKey(uid);
	}

}
