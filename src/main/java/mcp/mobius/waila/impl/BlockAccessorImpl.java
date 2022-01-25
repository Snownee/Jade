package mcp.mobius.waila.impl;

import java.util.List;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.AccessorImpl;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.impl.ui.FluidStackElement;
import mcp.mobius.waila.impl.ui.ItemStackElement;
import mcp.mobius.waila.network.RequestTilePacket;
import mcp.mobius.waila.overlay.RayTracing;
import mcp.mobius.waila.utils.WailaExceptionHandler;
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

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<BlockHitResult> implements BlockAccessor {

	private final BlockState blockState;
	private final BlockEntity blockEntity;

	public BlockAccessorImpl(BlockState blockState, BlockEntity blockEntity, Level level, Player player, CompoundTag serverData, BlockHitResult hit, boolean serverConnected) {
		super(level, player, serverData, hit, serverConnected);
		this.blockState = blockState;
		this.blockEntity = blockEntity;
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

		ItemStack pick = getPickedResult();
		if (!pick.isEmpty())
			icon = ItemStackElement.of(pick);

		if (RayTracing.isEmptyElement(icon) && getBlock().asItem() != Items.AIR)
			icon = ItemStackElement.of(new ItemStack(getBlock()));

		if (RayTracing.isEmptyElement(icon) && getBlock() instanceof LiquidBlock) {
			LiquidBlock block = (LiquidBlock) getBlock();
			Fluid fluid = block.getFluid();
			FluidStack fluidStack = new FluidStack(fluid, 1);
			icon = new FluidStackElement(fluidStack);//.size(new Size(18, 18));
		}

		for (IComponentProvider provider : WailaRegistrar.INSTANCE.getBlockIconProviders(getBlock())) {
			try {
				IElement element = provider.getIcon(this, PluginConfig.INSTANCE, icon);
				if (!RayTracing.isEmptyElement(element))
					icon = element;
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), null);
			}
		}
		return icon;
	}

	@Override
	public void _gatherComponents(ITooltip tooltip) {
		List<IComponentProvider> providers = WailaRegistrar.INSTANCE.getBlockProviders(getBlock(), getTooltipPosition());
		for (IComponentProvider provider : providers) {
			try {
				provider.appendTooltip(tooltip, this, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
	}

	@Override
	public boolean shouldDisplay() {
		return Waila.CONFIG.get().getGeneral().getDisplayBlocks();
	}

	@Override
	public void _requestData(boolean showDetails) {
		Waila.NETWORK.sendToServer(new RequestTilePacket(blockEntity, showDetails));
	}

	@Override
	public boolean shouldRequestData() {
		if (blockEntity == null)
			return false;
		return !WailaRegistrar.INSTANCE.getBlockNBTProviders(blockEntity).isEmpty();
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

}
