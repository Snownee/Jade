package mcp.mobius.waila.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;

/**
 * Class to get information of entity target and context.
 */
public class EntityAccessor extends Accessor {

	private final Entity entity;

	public EntityAccessor(Entity entity, World world, PlayerEntity player, CompoundNBT serverData, EntityRayTraceResult hit, boolean serverConnected) {
		super(world, player, serverData, hit, serverConnected);
		this.entity = entity;
	}

	@Override
	public EntityRayTraceResult getHitResult() {
		return (EntityRayTraceResult) super.getHitResult();
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public ItemStack getPickedResult() {
		return getEntity().getPickedResult(getHitResult());
	}
}
