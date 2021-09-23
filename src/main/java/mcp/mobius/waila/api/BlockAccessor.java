package mcp.mobius.waila.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

}
