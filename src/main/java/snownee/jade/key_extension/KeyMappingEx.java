package snownee.jade.key_extension;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public interface KeyMappingEx {
	boolean keyEx$isActive();

	void keyEx$setActive(boolean enabled);

	boolean keyEx$isNoConflict();

	void keyEx$setNoConflict(boolean noConflict);

	InputConstants.Key keyEx$key();

	static void setActive(KeyMapping keyMapping, boolean active) {
		((KeyMappingEx) keyMapping).keyEx$setActive(active);
	}

	static void setNoConflict(KeyMapping keyMapping, boolean noConflict) {
		((KeyMappingEx) keyMapping).keyEx$setNoConflict(noConflict);
	}
}
