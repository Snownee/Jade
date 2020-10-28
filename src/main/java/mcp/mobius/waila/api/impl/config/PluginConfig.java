package mcp.mobius.waila.api.impl.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginConfig implements IPluginConfig {

    public static final PluginConfig INSTANCE = new PluginConfig();

    private final Map<ResourceLocation, ConfigEntry> configs;

    private PluginConfig() {
        this.configs = Maps.newHashMap();
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
        File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), Waila.MODID + "/" + Waila.MODID + "_plugins.json");

        if (!configFile.exists()) { // Write defaults, but don't read
            writeConfig(configFile, true);
        } else { // Read back from config
            Map<String, Map<String, Boolean>> config;
            try (FileReader reader = new FileReader(configFile)) {
                config = new Gson().fromJson(reader, new TypeToken<Map<String, Map<String, Boolean>>>(){}.getType());
            } catch (IOException e) {
                config = Maps.newHashMap();
            }

            Set<ResourceLocation> found = Sets.newHashSet();
            config.forEach((namespace, subMap) -> subMap.forEach((path, value) -> {
                ResourceLocation id = new ResourceLocation(namespace, path);
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
        File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), Waila.MODID + "/" + Waila.MODID + "_plugins.json");
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
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
