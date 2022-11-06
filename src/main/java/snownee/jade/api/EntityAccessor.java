package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Class to get information of entity target and context.
 */
public interface EntityAccessor extends Accessor<EntityHitResult> {

	Entity getEntity();

	@NonExtendable
	interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		Builder hit(EntityHitResult hit);

		Builder entity(Entity entity);

		Builder from(EntityAccessor accessor);

		EntityAccessor build();
	}
}
