package snownee.jade.api.harvest;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.addon.harvest.SimpleToolType;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.callback.CallbackContainer;

/**
 * Groups a set of tool tiers that Jade can evaluate against a block state.
 */
public interface ToolType extends IJadeProvider {

	/**
	 * Tests the given block state for this tool type.
	 *
	 * @param state block state to test
	 * @param level current level
	 * @param pos block position
	 * @return the evaluation result
	 */
	ToolResult test(BlockState state, Level level, BlockPos pos);

	/**
	 * Returns the ordered tiers registered for this tool type.
	 *
	 * @return mutable tier list
	 */
	List<ToolTier> tiers();

	/**
	 * Returns the callback container that fires when a tier is added.
	 *
	 * @return tier-added callbacks
	 */
	CallbackContainer<ToolTierAddedCallback> tierAddedCallbacks();

	/**
	 * Returns the callback container that fires when a tier is removed.
	 *
	 * @return tier-removed callbacks
	 */
	CallbackContainer<ToolTierRemovedCallback> tierRemovedCallbacks();

	/**
	 * Notifies listeners that a tier was added.
	 *
	 * @param tier added tier
	 */
	default void onTierAdded(ToolTier tier) {
		tierAddedCallbacks().call($ -> $.onAdded(this, tier.getUid(), tier));
	}

	/**
	 * Notifies listeners that a tier was removed.
	 *
	 * @param tier removed tier
	 */
	default void onTierRemoved(ToolTier tier) {
		tierRemovedCallbacks().call($ -> $.onRemoved(this, tier.getUid(), tier));
	}

	/**
	 * Returns the tier with the given identifier.
	 *
	 * @param id tier identifier
	 * @return the matching tier or {@code null}
	 */
	@Nullable
	default ToolTier tier(Identifier id) {
		for (ToolTier tier : tiers()) {
			if (tier.getUid().equals(id)) {
				return tier;
			}
		}
		return null;
	}

	/**
	 * Appends a tier to the end of the ordered list.
	 *
	 * @param tier tier to add
	 * @return this tool type
	 */
	default ToolType addTier(ToolTier tier) {
		tiers().add(tier);
		onTierAdded(tier);
		return this;
	}

	/**
	 * Inserts a tier before another tier.
	 *
	 * @param targetTier existing tier identifier
	 * @param tier tier to insert
	 * @return {@code true} if the tier was inserted
	 */
	default boolean insertTierBefore(Identifier targetTier, ToolTier tier) {
		List<ToolTier> tiers = tiers();
		for (int i = 0; i < tiers.size(); i++) {
			if (tiers.get(i).getUid().equals(targetTier)) {
				tiers.add(i, tier);
				onTierAdded(tier);
				return true;
			}
		}
		return false;
	}

	/**
	 * Inserts a tier after another tier.
	 *
	 * @param targetTier existing tier identifier
	 * @param tier tier to insert
	 * @return {@code true} if the tier was inserted
	 */
	default boolean insertTierAfter(Identifier targetTier, ToolTier tier) {
		List<ToolTier> tiers = tiers();
		for (int i = 0; i < tiers.size(); i++) {
			if (tiers.get(i).getUid().equals(targetTier)) {
				tiers.add(i + 1, tier);
				onTierAdded(tier);
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the tier with the given identifier.
	 *
	 * @param tierId tier identifier
	 * @return {@code true} if a tier was removed
	 */
	default boolean removeTier(Identifier tierId) {
		List<ToolTier> tiers = tiers();
		List<ToolTier> removed = tiers.stream().filter(tier -> tier.getUid().equals(tierId)).toList();
		if (removed.isEmpty()) {
			return false;
		}
		tiers.removeAll(removed);
		removed.forEach(this::onTierRemoved);
		return true;
	}

	/**
	 * Removes the tier with the same identifier as the given tier.
	 *
	 * @param tier tier to remove
	 * @return {@code true} if a tier was removed
	 */
	default boolean removeTier(ToolTier tier) {
		return removeTier(tier.getUid());
	}

	/**
	 * Creates a mutable tool type with the given identifier.
	 *
	 * @param uid tool type identifier
	 * @return the tool type
	 */
	static ToolType of(Identifier uid) {
		return SimpleToolType.of(uid);
	}

	/**
	 * Creates a mutable tool type with the given identifier.
	 *
	 * @param uid tool type identifier
	 * @param skipInstaBreakingBlock whether instant-break blocks should be skipped
	 * @return the tool type
	 */
	static ToolType of(Identifier uid, boolean skipInstaBreakingBlock) {
		return SimpleToolType.of(uid, skipInstaBreakingBlock);
	}
}