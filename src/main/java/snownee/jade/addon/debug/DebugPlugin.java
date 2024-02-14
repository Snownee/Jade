package snownee.jade.addon.debug;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class DebugPlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(Identifiers.DEBUG_REGISTRY_NAME, RegistryNameProvider.Mode.OFF);
		registration.addConfig(Identifiers.DEBUG_SPECIAL_REGISTRY_NAME, false);

		registration.registerBlockComponent(BlockStatesProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(RegistryNameProvider.INSTANCE, Block.class);
		registration.registerEntityComponent(RegistryNameProvider.INSTANCE, Entity.class);

		registration.markAsClientFeature(Identifiers.DEBUG_BLOCK_STATES);
		registration.markAsClientFeature(Identifiers.DEBUG_REGISTRY_NAME);
		registration.markAsClientFeature(Identifiers.DEBUG_SPECIAL_REGISTRY_NAME);

		Component debug = Component.translatable("config.jade.plugin_jade.debug");
		registration.setConfigCategoryOverride(Identifiers.DEBUG_BLOCK_STATES, debug);
		registration.setConfigCategoryOverride(Identifiers.DEBUG_REGISTRY_NAME, debug);
	}
}
