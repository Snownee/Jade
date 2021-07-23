package mcp.mobius.waila.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessor extends Accessor {

	private final BlockState blockState;
	private final BlockEntity blockEntity;

	public BlockAccessor(BlockState blockState, BlockEntity tileEntity, Level world, Player player, CompoundTag serverData, BlockHitResult hit, boolean serverConnected) {
		super(world, player, serverData, hit, serverConnected);
		this.blockState = blockState;
		blockEntity = tileEntity;
	}

	public Block getBlock() {
		return getBlockState().getBlock();
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public BlockEntity getBlockEntity() {
		return blockEntity;
	}

	@Override
	public BlockHitResult getHitResult() {
		return (BlockHitResult) super.getHitResult();
	}

	public BlockPos getPosition() {
		return getHitResult().getBlockPos();
	}

	public Direction getSide() {
		return getHitResult().getDirection();
	}

	@Override
	public ItemStack getPickedResult() {
		return getBlockState().getPickBlock(getHitResult(), getLevel(), getPosition(), getPlayer());
	}

}
