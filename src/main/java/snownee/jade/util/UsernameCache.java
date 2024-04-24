package snownee.jade.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.config.PluginConfig;

public final class UsernameCache {

	public static final Codec<Map<UUID, String>> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, ExtraCodecs.NON_EMPTY_STRING);
	private static final int CACHE_SIZE = 1024;
	private static final Object2ObjectLinkedOpenHashMap<UUID, String> map = new Object2ObjectLinkedOpenHashMap<>(CACHE_SIZE);
	private static final Path saveFile = CommonProxy.getConfigDirectory().toPath().resolve(Jade.ID + "/usernamecache.json");
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static boolean loading = false;

	private UsernameCache() {
	}

	/**
	 * Set a player's current username
	 *
	 * @param uuid     the player's {@link java.util.UUID UUID}
	 * @param username the player's username
	 */
	public static void setUsername(UUID uuid, String username) {
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(username);

		if (!isValidName(username)) {
			return;
		}

		if (map.size() >= CACHE_SIZE) {
			map.removeFirst();
		}

		String prev = map.put(uuid, username);
		if (!loading && !Objects.equals(prev, username)) {
			save();
		}
	}

	public static boolean isValidName(String name) {
		return !name.isEmpty() && !name.contains("§");
	}

	/**
	 * Remove a player's username from the cache
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return if the cache contained the user
	 */
	public static boolean removeUsername(UUID uuid) {
		Objects.requireNonNull(uuid);

		if (map.remove(uuid) != null) {
			save();
			return true;
		}

		return false;
	}

	/**
	 * Get the player's last known username
	 * <p>
	 * <b>May be <code>null</code></b>
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return the player's last known username, or <code>null</code> if the
	 * cache doesn't have a record of the last username
	 */
	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		Objects.requireNonNull(uuid);
		String name = map.get(uuid);
		if (name == null && PluginConfig.INSTANCE.get(Identifiers.MC_ANIMAL_OWNER_FETCH_NAMES)) {
			name = SkullBlockEntity.fetchGameProfile(uuid).getNow(Optional.empty()).map(GameProfile::getName).orElse(null);
			if (name != null) {
				setUsername(uuid, name);
			}
		}
		return name;
	}

	/**
	 * Check if the cache contains the given player's username
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return if the cache contains a username for the given player
	 */
	public static boolean containsUUID(UUID uuid) {
		Objects.requireNonNull(uuid);
		return map.containsKey(uuid);
	}

	/**
	 * Get an immutable copy of the cache's underlying map
	 *
	 * @return the map
	 */
	public static Map<UUID, String> getMap() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Save the cache to file
	 */
	public static void save() {
		new SaveThread(gson.toJson(CODEC.encodeStart(JsonOps.INSTANCE, map))).start();
	}

	/**
	 * Load the cache from file
	 */
	public static void load() {
		if (!Files.exists(saveFile)) {
			return;
		}

		loading = true;
		try (final BufferedReader reader = Files.newBufferedReader(saveFile, StandardCharsets.UTF_8)) {
			JsonObject json = gson.fromJson(reader, JsonObject.class);
			Map<UUID, String> tempMap = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			if (tempMap != null) {
				map.clear();
				tempMap.forEach(UsernameCache::setUsername);
			}
		} catch (Exception e) {
			Jade.LOGGER.error("Could not parse username cache file as valid json, deleting file {}", saveFile, e);
			WailaExceptionHandler.handleErr(e, null, null);
			try {
				Files.delete(saveFile);
			} catch (IOException e1) {
				Jade.LOGGER.error("Could not delete file {}", saveFile.toString());
			}
		} finally {
			loading = false;
		}
	}

	/**
	 * Used for saving the {@link com.google.gson.Gson#toJson(Object) Gson}
	 * representation of the cache to disk
	 */
	private static class SaveThread extends Thread {

		/**
		 * The data that will be saved to disk
		 */
		private final String data;

		public SaveThread(String data) {
			this.data = data;
		}

		@Override
		public void run() {
			try {
				// Make sure we don't save when another thread is still saving
				synchronized (saveFile) {
					Files.writeString(saveFile, data, StandardCharsets.UTF_8);
				}
			} catch (IOException e) {
				Jade.LOGGER.error("Failed to save username cache to file!");
			}
		}
	}
}
