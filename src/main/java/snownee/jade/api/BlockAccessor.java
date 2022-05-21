package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

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

	@NonExtendable
	public interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder hit(BlockHitResult hit);

		Builder blockState(BlockState state);

		Builder blockEntity(BlockEntity blockEntity);

		Builder fakeBlock(ItemStack stack);

		Builder from(BlockAccessor accessor);

		BlockAccessor build();
	}

}
