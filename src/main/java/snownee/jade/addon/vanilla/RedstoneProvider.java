package snownee.jade.addon.vanilla;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class RedstoneProvider implements StreamServerDataProvider<BlockAccessor, Integer> {
	public static final RedstoneProvider INSTANCE = new RedstoneProvider();

	@Override
	public @Nullable Integer streamData(BlockAccessor accessor) {
		BlockEntity blockEntity = accessor.getBlockEntity();
		if (blockEntity instanceof ComparatorBlockEntity comparator) {
			return comparator.getOutputSignal();
		} else if (blockEntity instanceof CalibratedSculkSensorBlockEntity) {
			Direction direction = accessor.getBlockState().getValue(CalibratedSculkSensorBlock.FACING).getOpposite();
			return accessor.getLevel().getSignal(accessor.getPosition().relative(direction), direction);
		}
		return null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_REDSTONE;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			BlockState state = accessor.getBlockState();
			Block block = state.getBlock();
			IThemeHelper t = IThemeHelper.get();
			if (block instanceof LeverBlock) {
				Component info;
				if (state.getValue(BlockStateProperties.POWERED)) {
					info = t.success(Component.translatable("tooltip.jade.state_on"));
				} else {
					info = t.danger(Component.translatable("tooltip.jade.state_off"));
				}
				tooltip.add(Component.translatable("tooltip.jade.state", info));
				return;
			}

			if (block == Blocks.REPEATER) {
				int delay = state.getValue(BlockStateProperties.DELAY);
				tooltip.add(Component.translatable("tooltip.jade.delay", t.info(delay)));
				return;
			}

			Optional<Integer> signal = RedstoneProvider.INSTANCE.decodeFromData(accessor);
			if (block == Blocks.COMPARATOR) {
				ComparatorMode mode = state.getValue(BlockStateProperties.MODE_COMPARATOR);
				Component modeInfo = t.info(Component.translatable(
						"tooltip.jade.mode_" + (mode == ComparatorMode.COMPARE ? "comparator" : "subtractor")));
				tooltip.add(Component.translatable("tooltip.jade.mode", modeInfo));
				signal.ifPresent(i -> tooltip.add(Component.translatable("tooltip.jade.power", t.info(i))));
				return;
			}

			if (block instanceof CalibratedSculkSensorBlock && signal.isPresent()) {
				tooltip.add(Component.translatable("jade.input_signal", t.info(signal.get())));
			}

			if (state.hasProperty(BlockStateProperties.POWER)) {
				tooltip.add(Component.translatable("tooltip.jade.power", t.info(state.getValue(BlockStateProperties.POWER))));
			}
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_REDSTONE;
		}
	}
}
