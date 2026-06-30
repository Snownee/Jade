package snownee.jade.api.harvest;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.IJadeProvider;

/**
 * Describes a tool tier and how it matches block states.
 */
public interface ToolTier extends IJadeProvider {

	/**
	 * Creates a tier backed by a custom block-state predicate.
	 *
	 * @param uid tier identifier
	 * @param tool display stack for the tier
	 * @param predicate matching predicate
	 * @return the new tier
	 */
	static ToolTier of(Identifier uid, ItemStack tool, Predicate<BlockState> predicate) {
		Objects.requireNonNull(tool);
		return new SimpleToolTier(uid, tool, predicate);
	}

	/**
	 * Creates a tier backed by the given item.
	 *
	 * @param item item to display
	 * @return the new tier
	 */
	static ToolTier item(Item item) {
		return item(BuiltInRegistries.ITEM.getKey(item), item);
	}

	/**
	 * Creates a tier backed by the given item.
	 *
	 * @param uid tier identifier
	 * @param item item to display
	 * @return the new tier
	 */
	static ToolTier item(Identifier uid, Item item) {
		return item(uid, item.getDefaultInstance());
	}

	/**
	 * Creates a tier backed by the given item stack.
	 *
	 * @param uid tier identifier
	 * @param stack display stack
	 * @return the new tier
	 */
	static ToolTier item(Identifier uid, ItemStack stack) {
		return of(uid, stack, SimpleToolTier.isEffectiveTool(stack));
	}

	/**
	 * Creates a tier that always fails.
	 *
	 * @param item item to display
	 * @return the new tier
	 */
	static ToolTier alwaysFail(Item item) {
		return alwaysFail(BuiltInRegistries.ITEM.getKey(item), item);
	}

	/**
	 * Creates a tier that always fails.
	 *
	 * @param uid tier identifier
	 * @param item item to display
	 * @return the new tier
	 */
	static ToolTier alwaysFail(Identifier uid, Item item) {
		return alwaysFail(uid, item.getDefaultInstance());
	}

	/**
	 * Creates a tier that always fails.
	 *
	 * @param uid tier identifier
	 * @param stack display stack
	 * @return the new tier
	 */
	static ToolTier alwaysFail(Identifier uid, ItemStack stack) {
		return of(uid, stack, _ -> false);
	}

	/**
	 * Tests whether the tier is correct for the given block state.
	 *
	 * @param state block state to test
	 * @return the evaluation result
	 */
	ToolResult isCorrectTool(BlockState state);

	/**
	 * Adds additional blocks that should always match this tier.
	 *
	 * @param blocks blocks to add
	 * @return this tier
	 */
	ToolTier addExtraBlocks(Collection<? extends Block> blocks);

	/**
	 * Replaces the set of additional blocks that match this tier.
	 *
	 * @param blocks replacement block collection
	 * @return this tier
	 */
	ToolTier replaceExtraBlocks(Collection<? extends Block> blocks);
}
