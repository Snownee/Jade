package snownee.jade.key_extension;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public interface KeyMappingEx {
	boolean keyEx$isActive();

	void keyEx$setActive(boolean enabled);

	InputConstants.Key keyEx$key();

	static void setActive(KeyMapping keyMapping, boolean active) {
		((KeyMappingEx) keyMapping).keyEx$setActive(active);
	}
}
