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
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.Jade;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.network.RequestTilePacket;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.WailaExceptionHandler;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<BlockHitResult> implements BlockAccessor {

	private final BlockState blockState;
	private final BlockEntity blockEntity;
	private final ItemStack fakeBlock;

	public BlockAccessorImpl(BlockState blockState, BlockEntity blockEntity, Level level, Player player, CompoundTag serverData, BlockHitResult hit, boolean serverConnected, ItemStack fakeBlock) {
		super(level, player, serverData, hit, serverConnected);
		this.blockState = blockState;
		this.blockEntity = blockEntity;
		this.fakeBlock = fakeBlock;
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
		return getBlockState().getCloneItemStack(getHitResult(), getLevel(), getPosition(), getPlayer());
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

		if (RayTracing.isEmptyElement(icon) && getBlock().asItem() != Items.AIR)
			icon = ItemStackElement.of(new ItemStack(getBlock()));

		if (RayTracing.isEmptyElement(icon) && getBlock() instanceof LiquidBlock) {
			LiquidBlock block = (LiquidBlock) getBlock();
			Fluid fluid = block.getFluid();
			FluidStack fluidStack = new FluidStack(fluid, 1);
			icon = new FluidStackElement(fluidStack);//.size(new Size(18, 18));
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
				provider.appendTooltip(tooltip, this, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, tooltip);
			}
		}
	}

	@Override
	public boolean shouldDisplay() {
		return Jade.CONFIG.get().getGeneral().getDisplayBlocks();
	}

	@Override
	public void _requestData(boolean showDetails) {
		Jade.NETWORK.sendToServer(new RequestTilePacket(blockEntity, showDetails));
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

}
