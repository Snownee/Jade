package snownee.jade.addon.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwordToolHandler extends SimpleToolHandler {

	public SwordToolHandler(TagKey<Block> tagKey) {
		super("sword", tagKey, Items.WOODEN_SWORD);
		blocks.add(Blocks.BAMBOO);
		blocks.add(Blocks.BAMBOO_SAPLING);
		blocks.add(Blocks.COBWEB);
	}

	@Override
	public ItemStack test(BlockState state, Level world, BlockPos pos) {
		if (blocks.contains(state.getBlock())) {
			return tools.get(0);
		}
		return super.test(state, world, pos);
	}

	@Override
	public boolean matchesBlock(BlockState state) {
		return tag != null && state.is(tag);
	}
}
