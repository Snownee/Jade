package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getStack).<br>
 * An instance of this interface is passed to most of Waila Block/TileEntity callbacks.
 *
 * @author ProfMobius
 */

public interface IBlockAccessor extends IAccessor {

	Block getBlock();

	BlockState getBlockState();

	TileEntity getTileEntity();

	BlockPos getPosition();

	Direction getSide();

}
