package snownee.jade.impl;

import java.util.List;
import java.util.function.Function;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.WailaExceptionHandler;

public class BlockAccessorClientHandler implements Accessor.ClientHandler<BlockAccessor> {

	@Override
	public boolean shouldDisplay(BlockAccessor accessor) {
		return IWailaConfig.get().getGeneral().getDisplayBlocks();
	}

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		if (accessor.getBlockEntity() == null)
			return false;
		return !WailaCommonRegistration.INSTANCE.getBlockNBTProviders(accessor.getBlockEntity()).isEmpty();
	}

	@Override
	public void requestData(BlockAccessor accessor) {
		ClientProxy.requestBlockData(accessor);
	}

	@Override
	public IElement getIcon(BlockAccessor accessor) {
		BlockState blockState = accessor.getBlockState();
		Block block = blockState.getBlock();
		if (blockState.isAir())
			return null;
		IElement icon = null;

		if (accessor.isFakeBlock()) {
			icon = ItemStackElement.of(accessor.getFakeBlock());
		} else {
			ItemStack pick = accessor.getPickedResult();
			if (!pick.isEmpty())
				icon = ItemStackElement.of(pick);
		}

		if (RayTracing.isEmptyElement(icon) && block.asItem() != Items.AIR) {
			icon = ItemStackElement.of(new ItemStack(block));
		}

		if (RayTracing.isEmptyElement(icon) && block instanceof LiquidBlock) {
			icon = ClientProxy.elementFromLiquid((LiquidBlock) block);
		}

		for (IBlockComponentProvider provider : WailaClientRegistration.INSTANCE.getBlockIconProviders(block, PluginConfig.INSTANCE::get)) {
			try {
				IElement element = provider.getIcon(accessor, PluginConfig.INSTANCE, icon);
				if (!RayTracing.isEmptyElement(element))
					icon = element;
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, null);
			}
		}
		return icon;
	}

	@Override
	public void gatherComponents(BlockAccessor accessor, Function<IJadeProvider, ITooltip> tooltipProvider) {
		List<IBlockComponentProvider> providers = WailaClientRegistration.INSTANCE.getBlockProviders(accessor.getBlock(), PluginConfig.INSTANCE::get);
		for (IBlockComponentProvider provider : providers) {
			ITooltip tooltip = tooltipProvider.apply(provider);
			try {
				ElementHelper.INSTANCE.setCurrentUid(provider.getUid());
				provider.appendTooltip(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, tooltip);
			} finally {
				ElementHelper.INSTANCE.setCurrentUid(null);
			}
		}
	}
}
