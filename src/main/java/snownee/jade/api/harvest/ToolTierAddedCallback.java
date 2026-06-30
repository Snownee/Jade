package snownee.jade.api.harvest;

import net.minecraft.resources.Identifier;

/**
 * Notifies listeners that a tool tier was added to a tool type.
 */
@FunctionalInterface
public interface ToolTierAddedCallback {
	/**
	 * Called when a tier is registered.
	 *
	 * @param type tool type receiving the tier
	 * @param tierId tier identifier
	 * @param tier tier instance
	 */
	void onAdded(ToolType type, Identifier tierId, ToolTier tier);
}