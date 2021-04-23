package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockAccessor extends Accessor {

	private final BlockState blockState;
	private final TileEntity tileEntity;

	public BlockAccessor(BlockState blockState, TileEntity tileEntity, World world, PlayerEntity player, CompoundNBT serverData, BlockRayTraceResult hit, boolean serverConnected) {
		super(world, player, serverData, hit, serverConnected);
		this.blockState = blockState;
		this.tileEntity = tileEntity;
	}

	public Block getBlock() {
		return getBlockState().getBlock();
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public TileEntity getTileEntity() {
		return tileEntity;
	}

	@Override
	public BlockRayTraceResult getHitResult() {
		return (BlockRayTraceResult) super.getHitResult();
	}

	public BlockPos getPosition() {
		return getHitResult().getPos();
	}

	public Direction getSide() {
		return getHitResult().getFace();
	}

}
