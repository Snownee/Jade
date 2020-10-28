package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getStack).<br>
 * Common accessor for both Entity and Block/TileEntity.<br>
 * Available data depends on what it is called upon (ie : getEntity() will return null if looking at a block, etc).<br>
 */
public interface ICommonAccessor {

    World getWorld();

    PlayerEntity getPlayer();

    Block getBlock();

    ResourceLocation getBlockId();

    TileEntity getTileEntity();

    Entity getEntity();

    BlockPos getPosition();

    Vector3d getRenderingPosition();

    CompoundNBT getServerData();

    double getPartialFrame();

    Direction getSide();

    ItemStack getStack();
}
