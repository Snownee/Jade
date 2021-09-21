package mcp.mobius.waila.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraftforge.fml.loading.FMLPaths;

public class JsonConfig<T> {

	private static final Gson DEFAULT_GSON = new GsonBuilder().setPrettyPrinting().create();

	private final File configFile;
	private final CachedSupplier<T> configGetter;
	private Gson gson = DEFAULT_GSON;

	public JsonConfig(String fileName, Class<T> configClass, Supplier<T> defaultFactory) {
		this.configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), fileName + (fileName.endsWith(".json") ? "" : ".json"));
		this.configGetter = new CachedSupplier<>(() -> {
			if (!configFile.exists()) {
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
			try (BufferedReader reader = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
				return gson.fromJson(reader, configClass);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					configFile.renameTo(new File(configFile.getPath() + ".invalid"));
				} catch (Exception e1) {
				}
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
		});
	}

	public JsonConfig(String fileName, Class<T> configClass) {
		this(fileName, configClass, () -> {
			try {
				return configClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Failed to create new config instance", e);
			}
		});
	}

	public JsonConfig<T> withGson(Gson gson) {
		this.gson = gson;
		return this;
	}

	public T get() {
		return configGetter.get();
	}

	public void save() {
		write(get(), false); // Does not need to invalidate since the saved instance already has updated values
	}

	public void write(T t, boolean invalidate) {
		if (!configFile.getParentFile().exists())
			configFile.getParentFile().mkdirs();

		try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath(), StandardCharsets.UTF_8)) {
			writer.write(gson.toJson(t));
			if (invalidate)
				invalidate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void invalidate() {
		configGetter.invalidate();
	}

	static class CachedSupplier<T> {

		private final Supplier<T> supplier;
		private T value;

		public CachedSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			return value == null ? value = supplier.get() : value;
		}

		public void invalidate() {
			this.value = null;
		}
	}
}
