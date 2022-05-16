package snownee.jade.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Class to get information of entity target and context.
 */
public interface EntityAccessor extends Accessor<EntityHitResult> {

	Entity getEntity();

}
