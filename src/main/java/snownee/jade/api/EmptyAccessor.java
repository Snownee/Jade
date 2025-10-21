package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface EmptyAccessor extends Accessor<BlockHitResult> {

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return EmptyAccessor.class;
	}

	@NonExtendable
	interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		Builder hit(BlockHitResult hit);

		Builder from(EmptyAccessor accessor);

		default Builder requireVerification() {
			return requireVerification(true);
		}

		Builder requireVerification(boolean verify);

		EmptyAccessor build();
	}

}
