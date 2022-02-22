package snownee.jade.addon.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;

public class ShearsToolHandler extends SpecialToolHandler {

	public ShearsToolHandler() {
		super("shears", Items.SHEARS.getDefaultInstance());
		blocks.add(Blocks.GLOW_LICHEN);
		blocks.add(Blocks.TRIPWIRE);
		blocks.add(Blocks.WHITE_WOOL);
		blocks.add(Blocks.ORANGE_WOOL);
		blocks.add(Blocks.MAGENTA_WOOL);
		blocks.add(Blocks.LIGHT_BLUE_WOOL);
		blocks.add(Blocks.YELLOW_WOOL);
		blocks.add(Blocks.LIME_WOOL);
		blocks.add(Blocks.PINK_WOOL);
		blocks.add(Blocks.GRAY_WOOL);
		blocks.add(Blocks.LIGHT_GRAY_WOOL);
		blocks.add(Blocks.CYAN_WOOL);
		blocks.add(Blocks.PURPLE_WOOL);
		blocks.add(Blocks.BLUE_WOOL);
		blocks.add(Blocks.BROWN_WOOL);
		blocks.add(Blocks.GREEN_WOOL);
		blocks.add(Blocks.RED_WOOL);
		blocks.add(Blocks.BLACK_WOOL);
	}

	@Override
	public ItemStack test(BlockState state, Level world, BlockPos pos) {
		if (state.getBlock() instanceof IForgeShearable) {
			return tool;
		}
		return super.test(state, world, pos);
	}

}
