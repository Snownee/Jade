package snownee.jade.impl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.PlatformProxy;

public class PluginConfig implements IPluginConfig {

	public static final PluginConfig INSTANCE = new PluginConfig();
	public static final String FILE_NAME = Jade.MODID + "/plugins.json";

	private final Map<ResourceLocation, ConfigEntry> configs;

	private PluginConfig() {
		configs = Maps.newHashMap();
	}

	public void addConfig(ConfigEntry entry) {
		configs.put(entry.getId(), entry);
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
		ConfigEntry entry = configs.get(key);
		return entry == null ? defaultValue : entry.getValue();
	}

	public Set<ConfigEntry> getSyncableConfigs() {
		return configs.values().stream().filter(ConfigEntry::isSynced).collect(Collectors.toSet());
	}

	public List<String> getNamespaces() {
		return configs.keySet().stream().sorted((o1, o2) -> o1.getNamespace().compareToIgnoreCase(o2.getNamespace())).map(ResourceLocation::getNamespace).distinct().collect(Collectors.toList());
	}

	public ConfigEntry getEntry(ResourceLocation key) {
		return configs.get(key);
	}

	public void set(ResourceLocation key, boolean value) {
		ConfigEntry entry = configs.computeIfAbsent(key, k -> new ConfigEntry(k, value, true));
		entry.setValue(value);
	}

	public void reload() {
		File configFile = new File(PlatformProxy.getConfigDirectory(), FILE_NAME);

		if (!configFile.exists()) { // Write defaults, but don't read
			writeConfig(configFile, true);
		} else { // Read back from config
			Map<String, Map<String, Boolean>> config;
			try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
				config = new Gson().fromJson(reader, new TypeToken<Map<String, Map<String, Boolean>>>() {
				}.getType());
			} catch (Exception e) {
				e.printStackTrace();
				config = Maps.newHashMap();
			}

			Set<ResourceLocation> found = Sets.newHashSet();
			config.forEach((namespace, subMap) -> subMap.forEach((path, value) -> {
				ResourceLocation id = new ResourceLocation(namespace, path);
				if (!configs.containsKey(id)) {
					return;
				}
				set(id, value);
				found.add(id);
			}));

			Set<ResourceLocation> allKeys = getKeys();
			boolean flag = false;
			for (ResourceLocation id : allKeys) {
				if (!found.contains(id)) {
					set(id, getEntry(id).getDefaultValue());
					flag = true;
				}
			}

			if (flag)
				save();
		}
	}

	public void save() {
		File configFile = new File(PlatformProxy.getConfigDirectory(), FILE_NAME);
		writeConfig(configFile, false);
	}

	private void writeConfig(File file, boolean reset) {
		Map<String, Map<String, Boolean>> config = Maps.newHashMap();
		configs.values().forEach(e -> {
			Map<String, Boolean> modConfig = config.computeIfAbsent(e.getId().getNamespace(), k -> Maps.newHashMap());
			if (reset)
				e.setValue(e.getDefaultValue());
			modConfig.put(e.getId().getPath(), e.getValue());
		});

		String json = JsonConfig.DEFAULT_GSON.toJson(config);
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
}
