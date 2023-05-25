package snownee.jade.addon.vanilla;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum RedstoneProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Block block = state.getBlock();
		if (block instanceof LeverBlock) {
			boolean active = state.getValue(BlockStateProperties.POWERED);
			tooltip.add(Component.translatable("tooltip.jade.state", Component.translatable("tooltip.jade.state_" + (active ? "on" : "off"))));
			return;
		}

		if (block == Blocks.REPEATER) {
			int delay = state.getValue(BlockStateProperties.DELAY);
			tooltip.add(Component.translatable("tooltip.jade.delay", ChatFormatting.WHITE.toString() + delay));
			return;
		}

		if (block == Blocks.COMPARATOR) {
			ComparatorMode mode = state.getValue(BlockStateProperties.MODE_COMPARATOR);
			tooltip.add(Component.translatable("tooltip.jade.mode", Component.translatable("tooltip.jade.mode_" + (mode == ComparatorMode.COMPARE ? "comparator" : "subtractor")).withStyle(ChatFormatting.WHITE)));
			if (accessor.getServerData().contains("Signal")) {
				tooltip.add(Component.translatable("tooltip.jade.power", ChatFormatting.WHITE.toString() + accessor.getServerData().getInt("Signal")));
			}
			return;
		}

		if (state.hasProperty(BlockStateProperties.POWER)) {
			tooltip.add(Component.translatable("tooltip.jade.power", ChatFormatting.WHITE.toString() + state.getValue(BlockStateProperties.POWER)));
		}

		if (state.getBlock() instanceof HopperBlock && accessor.getServerData().contains("HopperLocked")) {
			tooltip.add(Component.translatable("jade.hopper.locked").withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		BlockEntity blockEntity = accessor.getBlockEntity();
		if (blockEntity instanceof ComparatorBlockEntity comparator) {
			data.putInt("Signal", comparator.getOutputSignal());
		} else if (blockEntity instanceof HopperBlockEntity) {
			BlockState state = blockEntity.getBlockState();
			if (state.hasProperty(BlockStateProperties.ENABLED) && !state.getValue(BlockStateProperties.ENABLED)) {
				data.putBoolean("HopperLocked", true);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_REDSTONE;
	}

}
