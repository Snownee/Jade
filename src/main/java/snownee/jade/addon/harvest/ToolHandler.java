package snownee.jade.addon.harvest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ToolHandler {

	ItemStack test(BlockState state, Level world, BlockPos pos);

	List<ItemStack> getTools();

	String getName();

}
