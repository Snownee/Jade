package snownee.jade.api.harvest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.IJadeProvider;

public interface ToolHandler extends IJadeProvider {

	ItemStack test(BlockState state, Level level, BlockPos pos);

	List<ItemStack> getTools();

}
