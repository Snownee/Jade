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

public interface ToolTier extends IJadeProvider {

	static ToolTier of(Identifier uid, ItemStack tool, Predicate<BlockState> predicate) {
		Objects.requireNonNull(tool);
		return new SimpleToolTier(uid, tool, predicate);
	}

	static ToolTier item(Item item) {
		return item(BuiltInRegistries.ITEM.getKey(item), item);
	}

	static ToolTier item(Identifier uid, Item item) {
		return item(uid, item.getDefaultInstance());
	}

	static ToolTier item(Identifier uid, ItemStack stack) {
		return of(uid, stack, SimpleToolTier.isEffectiveTool(stack));
	}

	static ToolTier alwaysFail(Item item) {
		return alwaysFail(BuiltInRegistries.ITEM.getKey(item), item);
	}

	static ToolTier alwaysFail(Identifier uid, Item item) {
		return alwaysFail(uid, item.getDefaultInstance());
	}

	static ToolTier alwaysFail(Identifier uid, ItemStack stack) {
		return of(uid, stack, _ -> false);
	}

	ToolResult isCorrectTool(BlockState state);

	ToolTier addExtraBlocks(Collection<? extends Block> blocks);

	ToolTier replaceExtraBlocks(Collection<? extends Block> blocks);
}
