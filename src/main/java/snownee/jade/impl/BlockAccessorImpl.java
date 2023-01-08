package snownee.jade.impl;

import java.util.List;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.Jade;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.ClientPlatformProxy;
import snownee.jade.util.WailaExceptionHandler;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<BlockHitResult> implements BlockAccessor {

	private final BlockState blockState;
	private final BlockEntity blockEntity;
	private ItemStack fakeBlock;

	private BlockAccessorImpl(Builder builder) {
		super(builder.level, builder.player, builder.serverData, builder.hit, builder.connected, builder.showDetails);
		blockState = builder.blockState;
		blockEntity = builder.blockEntity;
		fakeBlock = builder.fakeBlock;
	}

	@Override
	public Block getBlock() {
		return getBlockState().getBlock();
	}

	@Override
	public BlockState getBlockState() {
		return blockState;
	}

	@Override
	public BlockEntity getBlockEntity() {
		return blockEntity;
	}

	@Override
	public BlockPos getPosition() {
		return getHitResult().getBlockPos();
	}

	@Override
	public Direction getSide() {
		return getHitResult().getDirection();
	}

	@Override
	public ItemStack getPickedResult() {
		return ClientPlatformProxy.getBlockPickedResult(blockState, getPlayer(), getHitResult());
	}

	@Override
	public IElement _getIcon() {
		if (blockState.isAir())
			return null;
		IElement icon = null;

		if (isFakeBlock()) {
			icon = ItemStackElement.of(getFakeBlock());
		} else {
			ItemStack pick = getPickedResult();
			if (!pick.isEmpty())
				icon = ItemStackElement.of(pick);
		}

		if (RayTracing.isEmptyElement(icon) && getBlock().asItem() != Items.AIR) {
			icon = ItemStackElement.of(new ItemStack(getBlock()));
		}

		if (RayTracing.isEmptyElement(icon) && getBlock() instanceof LiquidBlock) {
			icon = ClientPlatformProxy.elementFromLiquid((LiquidBlock) getBlock());
		}

		for (IBlockComponentProvider provider : WailaClientRegistration.INSTANCE.getBlockIconProviders(getBlock(), PluginConfig.INSTANCE::get)) {
			try {
				IElement element = provider.getIcon(this, PluginConfig.INSTANCE, icon);
				if (!RayTracing.isEmptyElement(element))
					icon = element;
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, null);
			}
		}
		return icon;
	}

	@Override
	public void _gatherComponents(Function<IJadeProvider, ITooltip> tooltipProvider) {
		List<IBlockComponentProvider> providers = WailaClientRegistration.INSTANCE.getBlockProviders(getBlock(), PluginConfig.INSTANCE::get);
		for (IBlockComponentProvider provider : providers) {
			ITooltip tooltip = tooltipProvider.apply(provider);
			try {
				ElementHelper.INSTANCE.setCurrentUid(provider.getUid());
				provider.appendTooltip(tooltip, this, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, tooltip);
			} finally {
				ElementHelper.INSTANCE.setCurrentUid(null);
			}
		}
	}

	@Override
	public boolean shouldDisplay() {
		return Jade.CONFIG.get().getGeneral().getDisplayBlocks();
	}

	@Override
	public void _requestData() {
		ClientPlatformProxy.requestBlockData(blockEntity, showDetails());
	}

	@Override
	public boolean shouldRequestData() {
		if (blockEntity == null)
			return false;
		return !WailaCommonRegistration.INSTANCE.getBlockNBTProviders(blockEntity).isEmpty();
	}

	@Override
	public boolean _verifyData(CompoundTag serverData) {
		int x = serverData.getInt("x");
		int y = serverData.getInt("y");
		int z = serverData.getInt("z");
		BlockPos hitPos = getPosition();
		return x == hitPos.getX() && y == hitPos.getY() && z == hitPos.getZ();
	}

	@Override
	public Object _getTrackObject() {
		return getBlockEntity();
	}

	@Override
	public boolean isFakeBlock() {
		return !fakeBlock.isEmpty();
	}

	@Override
	public ItemStack getFakeBlock() {
		return fakeBlock;
	}

	public void setFakeBlock(ItemStack fakeBlock) {
		this.fakeBlock = fakeBlock;
	}

	public static class Builder implements BlockAccessor.Builder {

		private Level level;
		private Player player;
		private CompoundTag serverData;
		private boolean connected;
		private boolean showDetails = ClientPlatformProxy.isShowDetailsPressed();
		private BlockHitResult hit;
		private BlockState blockState = Blocks.AIR.defaultBlockState();
		private BlockEntity blockEntity;
		private ItemStack fakeBlock = ItemStack.EMPTY;

		@Override
		public Builder level(Level level) {
			this.level = level;
			return this;
		}

		@Override
		public Builder player(Player player) {
			this.player = player;
			return this;
		}

		@Override
		public Builder serverData(CompoundTag serverData) {
			this.serverData = serverData;
			return this;
		}

		@Override
		public Builder serverConnected(boolean connected) {
			this.connected = connected;
			return this;
		}

		@Override
		public Builder showDetails(boolean showDetails) {
			this.showDetails = showDetails;
			return this;
		}

		@Override
		public Builder hit(BlockHitResult hit) {
			this.hit = hit;
			return this;
		}

		@Override
		public Builder blockState(BlockState blockState) {
			this.blockState = blockState;
			return this;
		}

		@Override
		public Builder blockEntity(BlockEntity blockEntity) {
			this.blockEntity = blockEntity;
			return this;
		}

		@Override
		public Builder fakeBlock(ItemStack stack) {
			fakeBlock = stack;
			return this;
		}

		@Override
		public Builder from(BlockAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor.getHitResult();
			blockEntity = accessor.getBlockEntity();
			blockState = accessor.getBlockState();
			fakeBlock = accessor.getFakeBlock();
			return this;
		}

		@Override
		public BlockAccessor build() {
			return new BlockAccessorImpl(this);
		}

	}

}
