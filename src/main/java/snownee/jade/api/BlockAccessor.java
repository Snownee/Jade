package snownee.jade.api;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

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

	@Nullable
	BlockEntity getBlockEntity();

	default <T extends BlockEntity> T typedBlockEntity() {
		@SuppressWarnings("unchecked")
		T blockEntity = (T) getBlockEntity();
		return Objects.requireNonNull(blockEntity);
	}

	BlockPos getPosition();

	Direction getSide();

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return BlockAccessor.class;
	}

	@NonExtendable
	interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(@Nullable CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		Builder hit(BlockHitResult hit);

		Builder blockState(BlockState state);

		default Builder blockEntity(@Nullable BlockEntity blockEntity) {
			return blockEntity(() -> blockEntity);
		}

		Builder blockEntity(Supplier<@Nullable BlockEntity> blockEntity);

		Builder serversideRep(ItemStack stack);

		Builder from(BlockAccessor accessor);

		default Builder requireVerification() {
			return requireVerification(true);
		}

		Builder requireVerification(boolean verify);

		BlockAccessor build();
	}

}
