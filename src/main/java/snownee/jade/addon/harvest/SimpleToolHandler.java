package snownee.jade.addon.harvest;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleToolHandler implements ToolHandler {

	public final Set<Block> blocks = Sets.newIdentityHashSet();
	public final List<TagKey<Block>> blockTags = Lists.newArrayList();
	protected final List<ItemStack> tools = Lists.newArrayList();
	private final ResourceLocation uid;
	private final boolean testIsCorrectTool;

	public SimpleToolHandler(ResourceLocation uid, boolean testIsCorrectTool, List<ItemStack> tools) {
		this.uid = uid;
		this.testIsCorrectTool = testIsCorrectTool;
		if (testIsCorrectTool) {
			Preconditions.checkArgument(!tools.isEmpty(), "tools cannot be empty");
		} else {
			Preconditions.checkArgument(tools.size() == 1, "tools must have only one element");
		}
		this.tools.addAll(tools);
	}

	public static SimpleToolHandler create(ResourceLocation uid, boolean testIsCorrectTool, List<Item> tools) {
		return new SimpleToolHandler(uid, testIsCorrectTool, Lists.transform(tools, Item::getDefaultInstance));
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
			if (testIsCorrectTool) {
				if (!state.requiresCorrectToolForDrops() && state.getDestroySpeed(world, pos) == 0) {
					return ItemStack.EMPTY;
				}
				for (ItemStack tool : tools) {
					if (tool.isCorrectToolForDrops(state)) {
						return tool;
					}
				}
			} else {
				return tools.get(0);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public List<ItemStack> getTools() {
		return tools;
	}

	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	public SimpleToolHandler addBlock(Block block) {
		blocks.add(block);
		return this;
	}

	public SimpleToolHandler addBlockTag(TagKey<Block> tag) {
		blockTags.add(tag);
		return this;
	}
}
