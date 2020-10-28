package mcp.mobius.waila.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getStack).<br>
 * An instance of this interface is passed to most of Waila Entity callbacks.
 *
 * @author ProfMobius
 */

public interface IEntityAccessor {

    World getWorld();

    PlayerEntity getPlayer();

    Entity getEntity();

    RayTraceResult getHitResult();

    Vector3d getRenderingPosition();

     CompoundNBT getServerData();

    double getPartialFrame();
}
