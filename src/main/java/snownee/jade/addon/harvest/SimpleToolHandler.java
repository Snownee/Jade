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

	public final Set<Block> blocks = Sets.newIdentityHashSet();
	protected final List<ItemStack> tools = Lists.newArrayList();
	protected final List<TagKey<Block>> blockTags;
	private final String name;

	public SimpleToolHandler(String name, TagKey<Block> blockTag, Item... tools) {
		this(name, List.of(blockTag), tools);
	}

	public SimpleToolHandler(String name, List<TagKey<Block>> blockTags, Item... tools) {
		this.blockTags = blockTags;
		this.name = name;
		for (Item tool : tools) {
			this.tools.add(tool.getDefaultInstance());
		}
	}

	public boolean matchesBlock(BlockState state) {
		if (blocks.contains(state.getBlock())) {
			return true;
		}
		return blockTags.stream().anyMatch(state::is);
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
