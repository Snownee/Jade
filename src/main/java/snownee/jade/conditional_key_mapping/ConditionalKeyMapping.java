package snownee.jade.conditional_key_mapping;

import net.minecraft.client.KeyMapping;

public interface ConditionalKeyMapping {
	boolean conditionalKeyMapping$isEnabled();

	void conditionalKeyMapping$setEnabled(boolean enabled);

	static void set(KeyMapping keyMapping, boolean enabled) {
		((ConditionalKeyMapping) keyMapping).conditionalKeyMapping$setEnabled(enabled);
	}
}
