package snownee.jade.api.harvest;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleToolTier implements ToolTier {
	private final Identifier uid;
	private final ToolResult success;
	private final List<Block> extraBlocks = Lists.newArrayListWithExpectedSize(0);
	private final Predicate<BlockState> predicate;

	public SimpleToolTier(Identifier uid, ItemStack tool, Predicate<BlockState> predicate) {
		Objects.requireNonNull(uid);
		Objects.requireNonNull(tool);
		Objects.requireNonNull(predicate);
		this.uid = uid;
		this.success = ToolResult.of(tool);
		this.predicate = predicate;
	}

	@Override
	public Identifier getUid() {
		return uid;
	}

	@Override
	public ToolResult isCorrectTool(BlockState state) {
		return extraBlocks.contains(state.getBlock()) || predicate.test(state) ? success : ToolResult.pass();
	}

	@Override
	public ToolTier addExtraBlocks(Collection<? extends Block> blocks) {
		extraBlocks.addAll(blocks);
		return this;
	}

	@Override
	public ToolTier replaceExtraBlocks(Collection<? extends Block> blocks) {
		extraBlocks.clear();
		extraBlocks.addAll(blocks);
		return this;
	}

	public static Predicate<BlockState> isEffectiveTool(ItemStack toolItem) {
		Tool tool = toolItem.get(DataComponents.TOOL);
		if (tool == null) {
			return toolItem::isCorrectToolForDrops;
		}
		return state -> {
			for (Tool.Rule rule : tool.rules()) {
				if (rule.correctForDrops().isPresent() && state.is(rule.blocks())) {
					return rule.correctForDrops().get();
				}
			}
			if (tool.getMiningSpeed(state) > tool.defaultMiningSpeed()) {
				return true;
			}
			return toolItem.isCorrectToolForDrops(state);
		};
	}
}
