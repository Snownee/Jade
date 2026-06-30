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
 * Accessor describing the block currently under the Jade crosshair.
 */
public interface BlockAccessor extends Accessor<BlockHitResult> {

	/**
	 * Returns the resolved block.
	 *
	 * @return the target block
	 */
	Block getBlock();

	/**
	 * Returns the target block state.
	 *
	 * @return the current block state
	 */
	BlockState getBlockState();

	/**
	 * Returns the target block entity, if present.
	 *
	 * @return the block entity or {@code null}
	 */
	@Nullable
	BlockEntity getBlockEntity();

	/**
	 * Returns the block entity cast to a specific subtype.
	 *
	 * @param <T> block entity subtype
	 * @return the block entity
	 * @throws NullPointerException if the target has no block entity
	 * @throws ClassCastException if the block entity is not of the requested type
	 */
	default <T extends BlockEntity> T typedBlockEntity() {
		@SuppressWarnings("unchecked")
		T blockEntity = (T) getBlockEntity();
		return Objects.requireNonNull(blockEntity);
	}

	/**
	 * Returns the block position.
	 *
	 * @return the target position
	 */
	BlockPos getPosition();

	/**
	 * Returns the side that was hit.
	 *
	 * @return the hit face
	 */
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

		/**
		 * Sets the target block entity using a fixed instance.
		 *
		 * @param blockEntity the block entity or {@code null}
		 * @return this builder
		 */
		default Builder blockEntity(@Nullable BlockEntity blockEntity) {
			return blockEntity(() -> blockEntity);
		}

		/**
		 * Sets the target block entity supplier.
		 *
		 * @param blockEntity supplier for the block entity
		 * @return this builder
		 */
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
