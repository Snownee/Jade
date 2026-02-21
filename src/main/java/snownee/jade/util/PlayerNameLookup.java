package snownee.jade.util;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.authlib.GameProfile;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.Services;
import net.minecraft.util.Util;

public class PlayerNameLookup {
	private static final Set<UUID> FETCHING = ConcurrentHashMap.newKeySet();
	private static final HashMap<UUID, String> FETCHED = new HashMap<>();
	private static final GameProfile DUMMY_PROFILE = new GameProfile(UUID.randomUUID(), "???");

	public static boolean isFetching(UUID uuid) {
		return FETCHING.contains(uuid);
	}

	public static boolean isFetched(UUID uuid) {
		return FETCHED.containsKey(uuid);
	}

	@Nullable
	public static String get(@Nullable UUID uuid, Services services) {
		if (uuid == null) {
			return null;
		}
		if (FETCHED.containsKey(uuid)) {
			return FETCHED.get(uuid);
		}
		if (!FETCHING.add(uuid)) {
			return null;
		}
		CompletableFuture.runAsync(
				() -> {
					GameProfile profile = services.profileResolver().fetchById(uuid).orElse(DUMMY_PROFILE);
					FETCHED.put(uuid, profile.name());
					FETCHING.remove(uuid);
				}, Util.backgroundExecutor()
		);
		return null;
	}
}
