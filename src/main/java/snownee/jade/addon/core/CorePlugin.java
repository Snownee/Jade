package snownee.jade.addon.core;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.TargetBlocklist;
import snownee.jade.util.JsonConfig;

@WailaPlugin
public class CorePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.INSTANCE, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(Identifiers.CORE_DISTANCE, false);
		registration.addConfig(Identifiers.CORE_COORDINATES, false);
		registration.addConfig(Identifiers.CORE_REL_COORDINATES, false);
		registration.addConfig(Identifiers.CORE_REGISTRY_NAME, RegistryNameProvider.Mode.OFF);

		registration.registerBlockComponent(ObjectNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(RegistryNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ModNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(DistanceProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(BlockFaceProvider.INSTANCE, Block.class);

		registration.registerEntityComponent(ObjectNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(RegistryNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(ModNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(DistanceProvider.INSTANCE, Entity.class);

		registration.markAsClientFeature(Identifiers.CORE_DISTANCE);
		registration.markAsClientFeature(Identifiers.CORE_COORDINATES);
		registration.markAsClientFeature(Identifiers.CORE_REL_COORDINATES);
		registration.markAsClientFeature(Identifiers.CORE_REGISTRY_NAME);
		registration.markAsClientFeature(Identifiers.CORE_MOD_NAME);
		registration.markAsClientFeature(Identifiers.CORE_BLOCK_FACE);

		JsonConfig<TargetBlocklist> entityBlocklist = new JsonConfig<>(Jade.MODID + "/hide-entities-1902", TargetBlocklist.class, null, () -> {
			var blocklist = new TargetBlocklist();
			blocklist.values = Stream.of(EntityType.AREA_EFFECT_CLOUD, EntityType.FIREWORK_ROCKET)
					.map(EntityType::getKey)
					.map(Object::toString)
					.toList();
			return blocklist;
		});
		for (String id : entityBlocklist.get().values) {
			Registry.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(id)).ifPresent(registration::hideTarget);
		}
		JsonConfig<TargetBlocklist> blockBlocklist = new JsonConfig<>(Jade.MODID + "/hide-blocks-1902", TargetBlocklist.class, null, () -> {
			var blocklist = new TargetBlocklist();
			blocklist.values = List.of("minecraft:barrier");
			return blocklist;
		});
		for (String id : blockBlocklist.get().values) {
			Registry.BLOCK.getOptional(ResourceLocation.tryParse(id)).ifPresent(registration::hideTarget);
		}
	}
}
