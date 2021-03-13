package mcp.mobius.waila.api;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getStack).<br>
 * Common accessor for both Entity and Block/TileEntity.<br>
 * Available data depends on what it is called upon (ie : getEntity() will return null if looking at a block, etc).<br>
 */
public interface IAccessor {

    World getWorld();

    PlayerEntity getPlayer();

    CompoundNBT getServerData();

    RayTraceResult getHitResult();

    @Nullable
    TooltipPosition getTooltipPosition();

    boolean isServerConnected();
}
