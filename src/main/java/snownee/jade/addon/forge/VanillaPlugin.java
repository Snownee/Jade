package snownee.jade.addon.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class VanillaPlugin implements IWailaPlugin {

	private static ResourceLocation MC(String path) {
		return new ResourceLocation(path);
	}

	public static final ResourceLocation INVENTORY = MC("inventory");
	public static final ResourceLocation FORGE_ENERGY = MC("fe");
	public static final ResourceLocation FORGE_FLUID = MC("fluid");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(InventoryProvider.INSTANCE, BlockEntity.class);
		registration.registerBlockDataProvider(ForgeCapabilityProvider.INSTANCE, BlockEntity.class);

		registration.addConfig(INVENTORY, true);
		registration.addConfig(FORGE_ENERGY, true);
		registration.addConfig(FORGE_FLUID, true);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerComponentProvider(InventoryProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(ForgeCapabilityProvider.INSTANCE, TooltipPosition.BODY, Block.class);
	}

}
