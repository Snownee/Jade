package snownee.jade.test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import snownee.jade.Jade;

@Mod(Jade.ID)
public class Test {

	public Test(IEventBus eventBus) {
		eventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
			event.registerBlock(Capabilities.ItemHandler.BLOCK, new IBlockCapabilityProvider<>() {
				@Override
				public @NotNull IItemHandler getCapability(
						Level level,
						BlockPos blockPos,
						BlockState blockState,
						@Nullable BlockEntity blockEntity,
						@Nullable Direction direction) {
					return new ForwardingItemHandler(() -> new SnowItemHandler(level.getBlockState(blockPos), level, blockPos));
				}
			}, Blocks.SNOW);
		});
	}

	public static class SnowItemHandler extends ItemStackHandler {
		private final Level level;
		private final BlockPos blockPos;

		public SnowItemHandler(BlockState blockState, Level level, BlockPos blockPos) {
			super(NonNullList.of(
					ItemStack.EMPTY,
					Items.SNOW.getDefaultInstance().copyWithCount(blockState.getValue(BlockStateProperties.LAYERS))));
			this.level = level;
			this.blockPos = blockPos;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.is(Items.SNOW);
		}

		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 8;
		}

		@Override
		protected void onContentsChanged(int slot) {
			level.setBlockAndUpdate(
					blockPos,
					Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, getStackInSlot(0).getCount()));
		}
	}
}