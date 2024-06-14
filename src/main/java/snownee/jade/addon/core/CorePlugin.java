package snownee.jade.addon.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.impl.BlockAccessorClientHandler;
import snownee.jade.impl.EntityAccessorClientHandler;

@WailaPlugin
public class CorePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.getBlock(), BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerAccessorHandler(BlockAccessor.class, new BlockAccessorClientHandler());
		registration.registerAccessorHandler(EntityAccessor.class, new EntityAccessorClientHandler());

		registration.addConfig(JadeIds.CORE_DISTANCE, false);
		registration.addConfig(JadeIds.CORE_COORDINATES, false);
		registration.addConfig(JadeIds.CORE_REL_COORDINATES, false);

		registration.registerBlockComponent(ObjectNameProvider.getBlock(), Block.class);
		registration.registerBlockComponent(ModNameProvider.getBlock(), Block.class);
		registration.registerBlockComponent(DistanceProvider.getBlock(), Block.class);
		registration.registerBlockComponent(BlockFaceProvider.INSTANCE, Block.class);

		registration.registerEntityComponent(ObjectNameProvider.getEntity(), Entity.class);
		registration.registerEntityComponent(ModNameProvider.getEntity(), Entity.class);
		registration.registerEntityComponent(DistanceProvider.getEntity(), Entity.class);

		registration.markAsClientFeature(JadeIds.CORE_DISTANCE);
		registration.markAsClientFeature(JadeIds.CORE_COORDINATES);
		registration.markAsClientFeature(JadeIds.CORE_REL_COORDINATES);
		registration.markAsClientFeature(JadeIds.CORE_MOD_NAME);
		registration.markAsClientFeature(JadeIds.CORE_BLOCK_FACE);
	}
}
