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
	private static final Set<UUID> FETCHING = ConcurrentHashMap.newKeySet();
	private static final Set<UUID> FETCHED = ConcurrentHashMap.newKeySet();

	@Nullable
	public static String get(@Nullable UUID uuid, Services services) {
		if (uuid == null) {
			return null;
		}
		if (FETCHED.contains(uuid)) {
			return services.nameToIdCache().get(uuid).map(NameAndId::name).orElse("???");
		}
		if (!FETCHING.add(uuid)) {
			return null;
		}
		CompletableFuture.runAsync(
				() -> {
					services.profileResolver().fetchById(uuid);
					FETCHED.add(uuid);
					FETCHING.remove(uuid);
				}, Util.backgroundExecutor()
		);
		return null;
	}
}
