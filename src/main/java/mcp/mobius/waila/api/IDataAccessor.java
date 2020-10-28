package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getStack).<br>
 * An instance of this interface is passed to most of Waila Block/TileEntity callbacks.
 *
 * @author ProfMobius
 */

public interface IDataAccessor {

    World getWorld();

    PlayerEntity getPlayer();

    Block getBlock();

    BlockState getBlockState();

    TileEntity getTileEntity();

    RayTraceResult getHitResult();

    BlockPos getPosition();

    Vector3d getRenderingPosition();

    CompoundNBT getServerData();

    double getPartialFrame();

    Direction getSide();

    ItemStack getStack();
}
