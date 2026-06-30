package snownee.jade.api.harvest;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface ToolTierAddedCallback {
	void onAdded(ToolType type, Identifier tierId, ToolTier tier);
}