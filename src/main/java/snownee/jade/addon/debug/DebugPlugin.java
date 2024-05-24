package snownee.jade.addon.debug;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class DebugPlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(JadeIds.DEBUG_REGISTRY_NAME, RegistryNameProvider.Mode.OFF);
		registration.addConfig(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME, false);

		registration.registerBlockComponent(BlockPropertiesProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(BlockStatesProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(RegistryNameProvider.getBlock(), Block.class);
		registration.registerEntityComponent(RegistryNameProvider.getEntity(), Entity.class);

		registration.markAsClientFeature(JadeIds.DEBUG_BLOCK_STATES);
		registration.markAsClientFeature(JadeIds.DEBUG_BLOCK_PROPERTIES);
		registration.markAsClientFeature(JadeIds.DEBUG_REGISTRY_NAME);
		registration.markAsClientFeature(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME);

		Component debug = Component.translatable("config.jade.plugin_jade.debug");
		registration.setConfigCategoryOverride(JadeIds.DEBUG_BLOCK_PROPERTIES, debug);
		registration.setConfigCategoryOverride(JadeIds.DEBUG_BLOCK_STATES, debug);
		registration.setConfigCategoryOverride(JadeIds.DEBUG_REGISTRY_NAME, debug);
	}
}
