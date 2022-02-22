package mcp.mobius.waila.impl.config;

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

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.config.WailaConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import snownee.jade.Jade;

public class PluginConfig implements IPluginConfig {

	public static final PluginConfig INSTANCE = new PluginConfig();

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
		File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), Jade.MODID + "/" + Jade.MODID + "_plugins.json");

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
		File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), Jade.MODID + "/" + Jade.MODID + "_plugins.json");
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

		String json = new GsonBuilder().setPrettyPrinting().create().toJson(config);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public WailaConfig getWailaConfig() {
		return Waila.CONFIG.get();
	}
}
