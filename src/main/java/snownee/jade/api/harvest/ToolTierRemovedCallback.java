package snownee.jade.api.harvest;

import net.minecraft.resources.Identifier;

/**
 * Notifies listeners that a tool tier was removed from a tool type.
 */
@FunctionalInterface
public interface ToolTierRemovedCallback {
	/**
	 * Called when a tier is removed.
	 *
	 * @param type tool type the tier was removed from
	 * @param tierId tier identifier
	 * @param tier removed tier instance
	 */
	void onRemoved(ToolType type, Identifier tierId, ToolTier tier);
}