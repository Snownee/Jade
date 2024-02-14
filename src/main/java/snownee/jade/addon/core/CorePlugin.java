package snownee.jade.addon.core;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.TargetBlocklist;
import snownee.jade.impl.BlockAccessorClientHandler;
import snownee.jade.impl.EntityAccessorClientHandler;
import snownee.jade.util.JsonConfig;

@WailaPlugin
public class CorePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.INSTANCE, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerAccessorHandler(BlockAccessor.class, new BlockAccessorClientHandler());
		registration.registerAccessorHandler(EntityAccessor.class, new EntityAccessorClientHandler());

		registration.addConfig(Identifiers.CORE_DISTANCE, false);
		registration.addConfig(Identifiers.CORE_COORDINATES, false);
		registration.addConfig(Identifiers.CORE_REL_COORDINATES, false);

		registration.registerBlockComponent(ObjectNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ModNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(DistanceProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(BlockFaceProvider.INSTANCE, Block.class);

		registration.registerEntityComponent(ObjectNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(ModNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(DistanceProvider.INSTANCE, Entity.class);

		registration.markAsClientFeature(Identifiers.CORE_DISTANCE);
		registration.markAsClientFeature(Identifiers.CORE_COORDINATES);
		registration.markAsClientFeature(Identifiers.CORE_REL_COORDINATES);
		registration.markAsClientFeature(Identifiers.CORE_MOD_NAME);
		registration.markAsClientFeature(Identifiers.CORE_BLOCK_FACE);

		for (String id : createEntityBlocklist().get().values) {
			BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(id)).ifPresent(registration::hideTarget);
		}
		for (String id : createBlockBlocklist().get().values) {
			BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(id)).ifPresent(registration::hideTarget);
		}
	}

	public static JsonConfig<TargetBlocklist> createEntityBlocklist() {
		return new JsonConfig<>(Jade.MODID + "/hide-entities", TargetBlocklist.class, null, () -> {
			var blocklist = new TargetBlocklist();
			blocklist.values = Stream.of(EntityType.AREA_EFFECT_CLOUD, EntityType.FIREWORK_ROCKET, EntityType.INTERACTION, EntityType.TEXT_DISPLAY)
					.map(EntityType::getKey)
					.map(Object::toString)
					.toList();
			return blocklist;
		});
	}

	public static JsonConfig<TargetBlocklist> createBlockBlocklist() {
		return new JsonConfig<>(Jade.MODID + "/hide-blocks", TargetBlocklist.class, null, () -> {
			var blocklist = new TargetBlocklist();
			blocklist.values = List.of("minecraft:barrier");
			return blocklist;
		});
	}
}
