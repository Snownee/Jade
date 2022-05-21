package snownee.jade.addon.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CorePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.INSTANCE, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(ObjectNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(RegistryNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ModNameProvider.INSTANCE, Block.class);

		registration.registerEntityComponent(ObjectNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(RegistryNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(ModNameProvider.INSTANCE, Entity.class);

		registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);
		registration.hideTarget(EntityType.FIREWORK_ROCKET);
	}
}
