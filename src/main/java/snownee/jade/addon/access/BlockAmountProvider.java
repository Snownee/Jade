package snownee.jade.addon.access;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class BlockAmountProvider implements IBlockComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(JadeIds.ACCESS_BLOCK_DETAILS)) {
			return;
		}
		BlockState blockState = accessor.getBlockState();
		int amount = -1;
		if (blockState.hasProperty(BlockStateProperties.PICKLES)) {
			amount = blockState.getValue(BlockStateProperties.PICKLES);
		} else if (blockState.hasProperty(BlockStateProperties.CANDLES)) {
			amount = blockState.getValue(BlockStateProperties.CANDLES);
		} else if (blockState.hasProperty(BlockStateProperties.EGGS)) {
			amount = blockState.getValue(BlockStateProperties.EGGS);
		}
		if (amount >= 0) {
			tooltip.add(Component.translatable("jade.access.block.amount", amount));
		}
		if (blockState.hasProperty(BlockStateProperties.BITES)) {
			tooltip.add(Component.translatable("jade.access.block.bites", blockState.getValue(BlockStateProperties.BITES)));
		}
		if (blockState.hasProperty(BlockStateProperties.LAYERS)) {
			tooltip.add(Component.translatable("jade.access.block.layers", blockState.getValue(BlockStateProperties.LAYERS)));
		}
		if (blockState.hasProperty(BlockStateProperties.LEVEL_CAULDRON)) {
			tooltip.add(Component.translatable("jade.access.block.level", blockState.getValue(BlockStateProperties.LEVEL_CAULDRON)));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_BLOCK_AMOUNT;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
