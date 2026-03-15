package snownee.jade.util;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.Services;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;

public class PlayerNameLookup {

	public static final String DUMMY_NAME = "???";

	private static final Set<UUID> FETCHING = ConcurrentHashMap.newKeySet();
	private static final ConcurrentHashMap<UUID, String> FETCHED = new ConcurrentHashMap<>();

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
		String name = services.nameToIdCache().get(uuid).map(NameAndId::name).orElse(null);
		if (name != null) {
			FETCHED.put(uuid, name);
			return name;
		}
		if (!FETCHING.add(uuid)) {
			return null;
		}
		CompletableFuture.runAsync(
				() -> services.profileResolver().fetchById(uuid).ifPresentOrElse(
						profile -> {
							FETCHED.put(uuid, profile.name());
							FETCHING.remove(uuid);
						}, () -> FETCHED.put(uuid, DUMMY_NAME)
				), Util.backgroundExecutor()
		);
		return null;
	}
}