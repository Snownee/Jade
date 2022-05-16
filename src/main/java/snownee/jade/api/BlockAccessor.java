package snownee.jade.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Class to get information of block target and context.
 */
public interface BlockAccessor extends Accessor<BlockHitResult> {

	Block getBlock();

	BlockState getBlockState();

	BlockEntity getBlockEntity();

	BlockPos getPosition();

	Direction getSide();

	/**
	 * The targeting block is a custom block created by data pack
	 */
	boolean isFakeBlock();

	ItemStack getFakeBlock();

}
