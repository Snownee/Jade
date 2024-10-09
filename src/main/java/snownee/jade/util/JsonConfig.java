package snownee.jade.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;

public class JsonConfig<T> {

	/* off */
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.serializeNulls()
			.enableComplexMapKeySerialization()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.setLenient()
			.create();
	/* on */

	private final File file;
	private final Codec<T> codec;
	private final CachedSupplier<T> configGetter;

	public JsonConfig(String fileName, Codec<T> codec, @Nullable Consumer<T> onUpdate, Supplier<T> defaultFactory) {
		this.file = new File(CommonProxy.getConfigDirectory(), fileName + (fileName.endsWith(".json") ? "" : ".json"));
		this.codec = codec;
		this.configGetter = new CachedSupplier<>(() -> {
			if (!file.exists()) {
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
			try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
				T ret = codec.parse(JsonOps.INSTANCE, GSON.fromJson(reader, JsonElement.class)).getOrThrow();
				if (ret == null) {
					ret = defaultFactory.get();
					write(ret, false);
				}
				return ret;
			} catch (Throwable e) {
				Jade.LOGGER.error("Failed to read config file %s".formatted(file), e);
				if (file.length() > 0) {
					try {
						//noinspection ResultOfMethodCallIgnored
						file.renameTo(new File(file.getPath() + ".invalid"));
					} catch (Exception ignored) {
					}
				}
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
		});
		configGetter.onUpdate = onUpdate;
		mkdirs();
	}

	public JsonConfig(String fileName, Codec<T> codec, @Nullable Consumer<T> onUpdate) {
		this(fileName, codec, onUpdate, () -> JadeCodecs.createFromEmptyMap(codec));
		JadeCodecs.createFromEmptyMap(codec); // make sure it works
	}

	public T get() {
		return configGetter.get();
	}

	public void save() {
		write(get(), false); // Does not need to invalidate since the saved instance already has updated values
	}

	public void write(T t, boolean invalidate) {
		mkdirs();

		try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
			writer.write(GSON.toJson(codec.encodeStart(JsonOps.INSTANCE, t).getOrThrow()));
			if (invalidate) {
				invalidate();
			}
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to write config file %s".formatted(file), e);
		}
	}

	private void mkdirs() {
		if (!file.getParentFile().exists()) {
			//noinspection ResultOfMethodCallIgnored
			file.getParentFile().mkdirs();
		}
	}

	public void invalidate() {
		configGetter.invalidate();
	}

	public File getFile() {
		return file;
	}

	static class CachedSupplier<T> {

		private final Supplier<T> supplier;
		private T value;
		private Consumer<T> onUpdate;

		public CachedSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			if (value == null) {
				synchronized (this) {
					T _value = value = supplier.get();
					Objects.requireNonNull(_value);
					if (onUpdate != null) {
						onUpdate.accept(_value);
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
