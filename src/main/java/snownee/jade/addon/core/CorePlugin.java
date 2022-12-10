package snownee.jade.addon.core;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.reflect.TypeToken;

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

		@SuppressWarnings("serial")
		Type type = new TypeToken<List<String>>() {
		}.getType();
		JsonConfig<List<String>> config = new JsonConfig<>(Jade.MODID + "/hide-entities", type, null, List::of);
		for (String id : config.get()) {
			EntityType.byString(id).ifPresent(registration::hideTarget);
		}
	}
}
