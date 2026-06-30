package snownee.jade.api.harvest;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import snownee.jade.api.callback.CallbackContainer;

/**
 * Mutable registry used to define Jade's tool types and tiers.
 */
public interface ToolTypeRegistry {
	/**
	 * Registers a tool type for the given tool type.
	 *
	 * @param type tool type
	 * @return the registered tool type
	 */
	ToolType type(ToolType type);

	/**
	 * Registers a tool type for the given identifier.
	 *
	 * @param id tool type identifier
	 * @return the registered tool type
	 */
	ToolType type(Identifier id);

	/**
	 * Registers a tool type for the given identifier.
	 *
	 * @param id tool type identifier
	 * @param skipInstaBreakingBlock whether instant-break blocks should be skipped
	 * @return the registered tool type
	 */
	ToolType type(Identifier id, boolean skipInstaBreakingBlock);

	/**
	 * Looks up a registered tool type.
	 *
	 * @param typeId tool type identifier
	 * @return the tool type or {@code null}
	 */
	@Nullable ToolType get(Identifier typeId);

	/**
	 * Inserts a tier after an existing tier.
	 *
	 * @param typeId tool type identifier
	 * @param targetTier existing tier identifier
	 * @param tier tier to insert
	 */
	void insertTierAfter(Identifier typeId, Identifier targetTier, ToolTier tier);

	/**
	 * Inserts a tier before an existing tier.
	 *
	 * @param typeId tool type identifier
	 * @param targetTier existing tier identifier
	 * @param tier tier to insert
	 */
	void insertTierBefore(Identifier typeId, Identifier targetTier, ToolTier tier);

	/**
	 * Returns the callback container for tier additions.
	 *
	 * @param typeId tool type identifier
	 * @return callback container
	 */
	CallbackContainer<ToolTierAddedCallback> tierAddedCallback(Identifier typeId);

	/**
	 * Returns the default shears tier.
	 *
	 * @return default shears tier
	 */
	ToolTier defaultShearsTier();
}
