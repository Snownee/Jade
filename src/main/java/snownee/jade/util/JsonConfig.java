package snownee.jade.util;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import snownee.jade.Jade;

public class JsonConfig<T> {

	/* off */
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.serializeNulls()
			.enableComplexMapKeySerialization()
			.setStrictness(Strictness.LENIENT)
			.create();
	/* on */

	private final File file;
	private final Codec<T> codec;
	private final CachedSupplier<T> configGetter;

	public JsonConfig(String fileName, Codec<@Nullable T> codec, @Nullable Consumer<T> onUpdate, Supplier<T> defaultFactory) {
		this.file = new File(CommonProxy.getConfigDirectory(), fileName + (fileName.endsWith(".json") ? "" : ".json"));
		this.codec = codec;
		this.configGetter = new CachedSupplier<>(() -> {
			if (!file.exists()) {
				T def = defaultFactory.get();
				write(file, def, false);
				return def;
			}
			try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
				T ret = codec.parse(JsonOps.INSTANCE, GSON.fromJson(reader, JsonElement.class)).getOrThrow();
				if (ret == null) {
					ret = defaultFactory.get();
					write(file, ret, false);
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
				write(file, def, false);
				return def;
			}
		});
		configGetter.onUpdate = onUpdate;
		mkdirs(file);
	}

	public JsonConfig(String fileName, Codec<T> codec, @Nullable Consumer<T> onUpdate) {
		this(fileName, codec, onUpdate, () -> JadeCodecs.createFromEmptyMap(codec));
		if (CommonProxy.isDevEnv()) {
			JadeCodecs.createFromEmptyMap(codec); // make sure it works
		}
	}

	public T get() {
		return configGetter.get();
	}

	public void save() {
		saveTo(getFile());
	}

	public void saveTo(File file) {
		write(file, get(), false); // Does not need to invalidate since the saved instance already has updated values
	}

	public void write(File file, T t, boolean invalidate) {
		mkdirs(file);
		Path target = file.toPath();
		Path temp = target.resolveSibling(file.getName() + ".tmp");

		try {
			String json = GSON.toJson(codec.encodeStart(JsonOps.INSTANCE, t).getOrThrow());
			Files.writeString(temp, json, StandardCharsets.UTF_8);
			try {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
			}
			if (invalidate) {
				invalidate();
			}
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to write config file %s".formatted(file), e);
			try {
				Files.deleteIfExists(temp);
			} catch (Throwable ignored) {
			}
		}
	}

	private void mkdirs(File file) {
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
		private volatile @Nullable T value;
		private @Nullable Consumer<T> onUpdate;

		public CachedSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			T v = value;
			if (v == null) {
				synchronized (this) {
					v = value;
					if (v == null) {
						v = supplier.get();
						Objects.requireNonNull(v);
						if (onUpdate != null) {
							onUpdate.accept(v);
						}
						value = v;
					}
				}
			}
			return v;
		}

		public void invalidate() {
			this.value = null;
		}
	}
}
