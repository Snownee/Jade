package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Accessor used when Jade needs context without a specific target.
 */
public interface EmptyAccessor extends Accessor<BlockHitResult> {

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return EmptyAccessor.class;
	}

	@NonExtendable
	interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(@Nullable CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		Builder hit(BlockHitResult hit);

		/**
		 * Copies values from another empty accessor.
		 *
		 * @param accessor source accessor
		 * @return this builder
		 */
		Builder from(EmptyAccessor accessor);

		default Builder requireVerification() {
			return requireVerification(true);
		}

		Builder requireVerification(boolean verify);

		EmptyAccessor build();
	}

}
