package snownee.jade.addon.core;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MineTravellingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.MineTravellingBlockEntity;
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
	public static final Component CRAFTMINE = Component.translatable("config.jade.plugin_25w14craftmine");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.getBlock(), BlockEntity.class);
		registration.registerBlockDataProvider(TravellingBlockProvider.INSTANCE, MineTravellingBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerAccessorHandler(BlockAccessor.class, new BlockAccessorClientHandler());
		registration.registerAccessorHandler(EntityAccessor.class, new EntityAccessorClientHandler());

		registration.addConfig(JadeIds.CORE_DISTANCE, false);
		registration.addConfig(JadeIds.CORE_COORDINATES, false);
		registration.addConfig(JadeIds.CORE_REL_COORDINATES, false);
		registration.addConfig(JadeIds.CORE_EXCHANGE_VALUE_MINIMUM, 0.03F, 0F, 1000F, false);

		registration.registerBlockComponent(ObjectNameProvider.getBlock(), Block.class);
		registration.registerBlockComponent(ModNameProvider.getBlock(), Block.class);
		registration.registerBlockComponent(DistanceProvider.getBlock(), Block.class);
		registration.registerBlockComponent(BlockFaceProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ExchangeValueProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(TravellingBlockProvider.INSTANCE, MineTravellingBlock.class);

		registration.registerEntityComponent(ObjectNameProvider.getEntity(), Entity.class);
		registration.registerEntityComponent(ModNameProvider.getEntity(), Entity.class);
		registration.registerEntityComponent(DistanceProvider.getEntity(), Entity.class);

		registration.markAsClientFeature(JadeIds.CORE_DISTANCE);
		registration.markAsClientFeature(JadeIds.CORE_COORDINATES);
		registration.markAsClientFeature(JadeIds.CORE_REL_COORDINATES);
		registration.markAsClientFeature(JadeIds.CORE_MOD_NAME);
		registration.markAsClientFeature(JadeIds.CORE_BLOCK_FACE);
		registration.markAsClientFeature(JadeIds.CORE_EXCHANGE_VALUE_MINIMUM);

		registration.setConfigCategoryOverride(JadeIds.CORE_EXCHANGE_VALUE_MINIMUM, CRAFTMINE);
		registration.setConfigCategoryOverride(JadeIds.CORE_TRAVELLING_BLOCK, CRAFTMINE);
	}
}
