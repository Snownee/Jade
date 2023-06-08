package snownee.jade.addon.harvest;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleToolHandler implements ToolHandler {

	private final String name;
	protected final List<ItemStack> tools = Lists.newArrayList();
	protected final TagKey<Block> tag;
	public final Set<Block> blocks = Sets.newIdentityHashSet();

	public SimpleToolHandler(String name, TagKey<Block> tag, Item... tools) {
		this.tag = tag;
		this.name = name;
		for (Item tool : tools) {
			this.tools.add(tool.getDefaultInstance());
		}
	}

	public boolean matchesBlock(BlockState state) {
		if (tag != null && state.is(tag)) {
			return true;
		}
		return blocks.contains(state.getBlock());
	}

	@Override
	public ItemStack test(BlockState state, Level world, BlockPos pos) {
		if (matchesBlock(state)) {
			for (ItemStack tool : tools) {
				if (tool.isCorrectToolForDrops(state)) {
					return tool;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public List<ItemStack> getTools() {
		return tools;
	}

	@Override
	public String getName() {
		return name;
	}

}
