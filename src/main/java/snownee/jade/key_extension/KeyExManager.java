package snownee.jade.key_extension;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public final class KeyExManager {
	private static final ListMultimap<InputConstants.Key, KeyMapping> MAP = ArrayListMultimap.create();
	private static List<KeyMapping> activeKeys = List.of();
	private static boolean globalNoConflict;
	private static boolean dirty;

	public static boolean isGlobalNoConflict() {
		return globalNoConflict;
	}

	public static void setGlobalNoConflict(boolean globalNoConflict) {
		KeyExManager.globalNoConflict = globalNoConflict;
		markDirty();
	}

	public static void markDirty() {
		dirty = true;
	}

	public static void click(InputConstants.Key key, @Nullable KeyMapping keyMapping) {
		for (KeyMapping mapping : MAP.get(key)) {
			if (mapping == keyMapping) {
				continue;
			}
			mapping.clickCount++;
		}
	}

	public static void set(InputConstants.Key key, boolean bl, @Nullable KeyMapping keyMapping) {
		for (KeyMapping mapping : MAP.get(key)) {
			if (mapping == keyMapping) {
				continue;
			}
			mapping.setDown(bl);
		}
	}

	public static void resetMapping(Map<String, KeyMapping> all) {
		activeKeys = all.values().stream()
				.filter(keyMapping -> ((KeyMappingEx) keyMapping).keyEx$isActive())
				.toList();
		MAP.clear();
		for (KeyMapping keyMapping : activeKeys) {
			if (!keyMapping.isUnbound() && ((KeyMappingEx) keyMapping).keyEx$isNoConflict()) {
				MAP.put(((KeyMappingEx) keyMapping).keyEx$key(), keyMapping);
			}
		}
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
