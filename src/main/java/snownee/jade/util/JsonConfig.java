package snownee.jade.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class JsonConfig<T> {

	/* off */
	public static final Gson DEFAULT_GSON = new GsonBuilder()
			.setPrettyPrinting()
			.serializeNulls()
			.enableComplexMapKeySerialization()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(Style.class, new CodecJsonSerializer<>(Style.Serializer.CODEC))
			.setLenient()
			.create();
	/* on */

	private final File configFile;
	private final CachedSupplier<T> configGetter;
	private Gson gson = DEFAULT_GSON;

	public JsonConfig(String fileName, Type configClass, @Nullable Runnable onUpdate, Supplier<T> defaultFactory) {
		this.configFile = new File(CommonProxy.getConfigDirectory(), fileName + (fileName.endsWith(".json") ? "" : ".json"));
		this.configGetter = new CachedSupplier<>(() -> {
			if (!configFile.exists()) {
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
			try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
				T ret = gson.fromJson(reader, configClass);
				if (ret == null) {
					ret = defaultFactory.get();
					write(ret, false);
				}
				return ret;
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
		configGetter.onUpdate = onUpdate;
	}

	public JsonConfig(String fileName, Class<T> configClass, @Nullable Runnable onUpdate) {
		this(fileName, configClass, onUpdate, () -> {
			try {
				return configClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
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
		if (!configFile.getParentFile().exists()) {
			configFile.getParentFile().mkdirs();
		}

		try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
			writer.write(gson.toJson(t));
			if (invalidate) {
				invalidate();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void invalidate() {
		configGetter.invalidate();
	}

	public File getFile() {
		return configFile;
	}

	static class CachedSupplier<T> {

		private final Supplier<T> supplier;
		private T value;
		private Runnable onUpdate;

		public CachedSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			if (value == null) {
				synchronized (this) {
					value = supplier.get();
					Objects.requireNonNull(value);
					if (onUpdate != null) {
						onUpdate.run();
					}
				}
			}
			return value;
		}

		public void invalidate() {
			this.value = null;
		}
	}
}
