package snownee.jade.impl;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.AccessorClientHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.JadeUIInternal;
import snownee.jade.network.RequestBlockPacket;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.WailaExceptionHandler;

public class BlockAccessorClientHandler implements AccessorClientHandler<BlockAccessor> {

	@Override
	public boolean shouldDisplay(BlockAccessor accessor) {
		return IWailaConfig.get().general().getDisplayBlocks();
	}

	@Override
	public List<IServerDataProvider<BlockAccessor>> shouldRequestData(BlockAccessor accessor) {
		List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.instance().getBlockNBTProviders(
				accessor.getBlock(),
				accessor.getBlockEntity());
		if (providers.isEmpty()) {
			return List.of();
		}
		return providers.stream().filter(provider -> {
			try {
				return provider.shouldRequestData(accessor);
			} catch (Exception e) {
				WailaExceptionHandler.handleErr(e, provider, null);
				return false;
			}
		}).toList();
	}

	@Override
	public void requestData(BlockAccessor accessor, List<IServerDataProvider<BlockAccessor>> providers) {
		ClientProxy.sendPacket(new RequestBlockPacket(new BlockAccessorImpl.SyncData(accessor), providers));
	}

	@Override
	public @Nullable Element getIcon(BlockAccessor accessor) {
		BlockState blockState = accessor.getBlockState();
		Block block = blockState.getBlock();
		if (blockState.isAir()) {
			return null;
		}
		Element icon = null;

		if (accessor.isFakeBlock()) {
			icon = JadeUI.item(accessor.getFakeBlock());
		} else {
			ItemStack pick = accessor.getPickedResult();
			if (!pick.isEmpty()) {
				icon = JadeUI.item(pick);
			}
		}

		if (JadeUI.isEmptyElement(icon) && block.asItem() != Items.AIR) {
			icon = JadeUI.item(new ItemStack(block));
		}

		if (JadeUI.isEmptyElement(icon) && block instanceof LiquidBlock) {
			icon = ClientProxy.elementFromLiquid(blockState);
		}

		for (var provider : WailaClientRegistration.instance().getBlockIconProviders(block, this::isEnabled)) {
			try {
				Element element = provider.getIcon(accessor, IWailaConfig.get().plugin(), icon);
				if (!JadeUI.isEmptyElement(element)) {
					icon = element;
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, null);
			}
		}
		return icon;
	}

	@Override
	public void gatherComponents(BlockAccessor accessor, Function<IJadeProvider, ITooltip> tooltipProvider) {
		for (var provider : WailaClientRegistration.instance().getBlockProviders(accessor.getBlock(), this::isEnabled)) {
			ITooltip tooltip = tooltipProvider.apply(provider);
			try {
				JadeUIInternal.setContextUid(provider.getUid());
				provider.appendTooltip(tooltip, accessor, IWailaConfig.get().plugin());
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, tooltip::add);
			} finally {
				JadeUIInternal.setContextUid(null);
			}
		}
	}
}
