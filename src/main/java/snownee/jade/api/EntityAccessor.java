package snownee.jade.api;

import java.util.function.Supplier;

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

	/**
	 * For part entity like ender dragon's, getEntity() will return the parent entity.
	 */
	//TODO 1.21: remove default implementation
	default Entity getRawEntity() {
		return getEntity();
	}

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return EntityAccessor.class;
	}

	@NonExtendable
	public interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		default Builder hit(EntityHitResult hit) {
			return hit(() -> hit);
		}

		Builder hit(Supplier<EntityHitResult> hit);

		default Builder entity(Entity entity) {
			return entity(() -> entity);
		}

		Builder entity(Supplier<Entity> entity);

		Builder from(EntityAccessor accessor);

		Builder requireVerification();

		EntityAccessor build();
	}
}
