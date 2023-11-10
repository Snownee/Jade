package snownee.jade.addon.harvest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.Identifiers;
import snownee.jade.util.CommonProxy;

public class ShearsToolHandler extends SimpleToolHandler {

	public ShearsToolHandler() {
		super(Identifiers.JADE("shears"), false, List.of(Items.SHEARS.getDefaultInstance()));
		blocks.addAll(List.of(
				Blocks.GLOW_LICHEN,
				Blocks.TRIPWIRE,
				Blocks.VINE,
				Blocks.SMALL_DRIPLEAF,
				Blocks.DEAD_BUSH,
				Blocks.COBWEB,
				Blocks.SHORT_GRASS,
				Blocks.TALL_GRASS,
				Blocks.FERN,
				Blocks.LARGE_FERN
		));
		blockTags.addAll(List.of(
				BlockTags.WOOL,
				BlockTags.LEAVES
		));
	}

	@Override
	public ItemStack test(BlockState state, Level world, BlockPos pos) {
		if (CommonProxy.isShearable(state)) {
			return tools.get(0);
		}
		return super.test(state, world, pos);
	}

}
