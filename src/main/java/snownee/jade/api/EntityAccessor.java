package snownee.jade.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Accessor describing the entity currently under the Jade crosshair.
 */
public interface EntityAccessor extends Accessor<EntityHitResult> {

	/**
	 * Returns the resolved entity.
	 *
	 * @return the target entity
	 */
	Entity getEntity();

	/**
	 * Returns the raw entity that was hit before any part-to-parent resolution.
	 *
	 * @return the raw target entity
	 */
	Entity getRawEntity();

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return EntityAccessor.class;
	}

	@NonExtendable
	interface Builder {
		Builder level(Level level);

		Builder player(Player player);

		Builder serverData(@Nullable CompoundTag serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		/**
		 * Sets the hit result supplier.
		 *
		 * @param hit supplier for the entity hit result
		 * @return this builder
		 */
		default Builder hit(EntityHitResult hit) {
			return hit(() -> hit);
		}

		/**
		 * Sets the hit result supplier.
		 *
		 * @param hit supplier for the entity hit result
		 * @return this builder
		 */
		Builder hit(Supplier<EntityHitResult> hit);

		/**
		 * Sets the entity supplier.
		 *
		 * @param entity supplier for the entity
		 * @return this builder
		 */
		default Builder entity(Entity entity) {
			return entity(() -> entity);
		}

		/**
		 * Sets the entity supplier.
		 *
		 * @param entity supplier for the entity
		 * @return this builder
		 */
		Builder entity(Supplier<Entity> entity);

		Builder from(EntityAccessor accessor);

		default Builder requireVerification() {
			return requireVerification(true);
		}

		Builder requireVerification(boolean verify);

		EntityAccessor build();
	}
}
