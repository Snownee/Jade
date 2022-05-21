package snownee.jade.addon.forge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ForgePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BlockInventoryProvider.INSTANCE, BlockEntity.class);
		registration.registerBlockDataProvider(ForgeFluidProvider.INSTANCE, BlockEntity.class);
		registration.registerBlockDataProvider(ForgeEnergyProvider.INSTANCE, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(BlockInventoryProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ForgeFluidProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ForgeEnergyProvider.INSTANCE, Block.class);
	}

}
