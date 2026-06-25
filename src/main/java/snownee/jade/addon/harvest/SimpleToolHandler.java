package snownee.jade.addon.harvest;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.harvest.MutableToolHandler;
import snownee.jade.api.harvest.ToolHandler;
import snownee.jade.api.harvest.ToolTier;

public class SimpleToolHandler implements ToolHandler, MutableToolHandler {

	protected final List<ToolTier> tiers = Lists.newArrayList();
	protected final List<Block> extraBlocks = Lists.newArrayListWithExpectedSize(0);
	private final Identifier uid;
	private final boolean skipInstaBreakingBlock;

	protected SimpleToolHandler(Identifier uid, List<ToolTier> tiers, boolean skipInstaBreakingBlock) {
		this.uid = uid;
		Preconditions.checkArgument(!tiers.isEmpty(), "tiers cannot be empty");
		this.tiers.addAll(tiers);
		this.skipInstaBreakingBlock = skipInstaBreakingBlock;
	}

	public static SimpleToolHandler create(Identifier uid, List<Item> tools) {
		return create(uid, tools, true);
	}

	public static SimpleToolHandler create(Identifier uid, List<Item> tools, boolean skipInstaBreakingBlock) {
		return new SimpleToolHandler(
				uid,
				Lists.transform(tools, item -> ToolTier.item(BuiltInRegistries.ITEM.getKey(item), item)),
				skipInstaBreakingBlock);
	}

	public static SimpleToolHandler createTiers(Identifier uid, List<ToolTier> tiers, boolean skipInstaBreakingBlock) {
		return new SimpleToolHandler(uid, tiers, skipInstaBreakingBlock);
	}

	public static SimpleToolHandler createStacks(Identifier uid, List<ItemStack> tools, boolean skipInstaBreakingBlock) {
		return new SimpleToolHandler(
				uid,
				Lists.transform(tools, stack -> ToolTier.stack(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack)),
				skipInstaBreakingBlock);
	}

	@Override
	public ItemStack test(BlockState state, Level world, BlockPos pos) {
		if (extraBlocks.contains(state.getBlock())) {
			return firstMatchingTool();
		}
		if (skipInstaBreakingBlock && !state.requiresCorrectToolForDrops() && state.getDestroySpeed(world, pos) == 0) {
			return ItemStack.EMPTY;
		}
		return testTiers(state);
	}

	protected ItemStack test(BlockState state) {
		return testTiers(state);
	}

	protected ItemStack firstMatchingTool() {
		for (ToolTier tier : tiers) {
			for (ItemStack toolItem : tier.getTools()) {
				if (tier.matches(toolItem)) {
					return toolItem;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	protected ItemStack testTiers(BlockState state) {
		for (ToolTier tier : tiers) {
			for (ItemStack toolItem : tier.getTools()) {
				if (isEffectiveTool(toolItem, state) && tier.matches(toolItem)) {
					return toolItem;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	protected boolean isEffectiveTool(ItemStack toolItem, BlockState state) {
		Tool tool = toolItem.get(DataComponents.TOOL);
		if (tool != null) {
			for (Tool.Rule rule : tool.rules()) {
				if (rule.correctForDrops().isPresent() && state.is(rule.blocks())) {
					return rule.correctForDrops().get();
				}
			}
			if (tool.getMiningSpeed(state) > tool.defaultMiningSpeed()) {
				return true;
			}
		}
		return toolItem.isCorrectToolForDrops(state);
	}

	@Override
	public List<ItemStack> getTools() {
		return tiers.stream().flatMap(tier -> tier.getTools().stream()).toList();
	}

	@Override
	public void add(ToolTier tier) {
		tiers.add(tier);
	}

	@Override
	public boolean insertBefore(Identifier targetTier, ToolTier tier) {
		return insert(targetTier, tier, 0);
	}

	@Override
	public boolean insertAfter(Identifier targetTier, ToolTier tier) {
		return insert(targetTier, tier, 1);
	}

	private boolean insert(Identifier targetTier, ToolTier tier, int offset) {
		for (int i = 0; i < tiers.size(); i++) {
			if (tiers.get(i).getUid().equals(targetTier)) {
				tiers.add(i + offset, tier);
				return true;
			}
		}
		return false;
	}

	@Override
	public Identifier getUid() {
		return uid;
	}

	public SimpleToolHandler addExtraBlock(Block block) {
		extraBlocks.add(block);
		return this;
	}
}
