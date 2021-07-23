package mcp.mobius.waila.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Class to get information of entity target and context.
 */
public class EntityAccessor extends Accessor {

	private final Entity entity;

	public EntityAccessor(Entity entity, Level world, Player player, CompoundTag serverData, EntityHitResult hit, boolean serverConnected) {
		super(world, player, serverData, hit, serverConnected);
		this.entity = entity;
	}

	@Override
	public EntityHitResult getHitResult() {
		return (EntityHitResult) super.getHitResult();
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public ItemStack getPickedResult() {
		return getEntity().getPickedResult(getHitResult());
	}
}
