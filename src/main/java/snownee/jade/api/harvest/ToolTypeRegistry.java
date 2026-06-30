package snownee.jade.api.harvest;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import snownee.jade.api.callback.CallbackContainer;

public interface ToolTypeRegistry {
	ToolType type(ToolType type);

	ToolType type(Identifier id);

	ToolType type(Identifier id, boolean skipInstaBreakingBlock);

	@Nullable ToolType get(Identifier typeId);

	void insertTierAfter(Identifier typeId, Identifier targetTier, ToolTier tier);

	void insertTierBefore(Identifier typeId, Identifier targetTier, ToolTier tier);

	CallbackContainer<ToolTierAddedCallback> tierAddedCallback(Identifier typeId);

	ToolTier defaultShearsTier();
}
