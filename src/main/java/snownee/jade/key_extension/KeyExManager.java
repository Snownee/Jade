package snownee.jade.key_extension;

import java.util.List;
import java.util.Map;

import net.minecraft.client.KeyMapping;

public final class KeyExManager {
	private static List<KeyMapping> activeKeys = List.of();
	private static boolean dirty;

	public static void markDirty() {
		dirty = true;
	}

	public static void resetMapping(Map<String, KeyMapping> all) {
		activeKeys = all.values().stream()
				.filter(keyMapping -> ((KeyMappingEx) keyMapping).keyEx$isActive())
				.toList();
		dirty = false;
	}

	public static List<KeyMapping> activeKeys() {
		return activeKeys;
	}

	public static void checkDirty() {
		if (dirty) {
			KeyMapping.resetMapping();
		}
	}
}
