package snownee.jade.impl;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.impl.config.ConfigEntry;
import snownee.jade.impl.config.PluginConfig;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	public static final WailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	public final HierarchyLookup<IServerDataProvider<BlockEntity>> blockDataProviders;
	public final HierarchyLookup<IServerDataProvider<Entity>> entityDataProviders;

	WailaCommonRegistration() {
		blockDataProviders = new HierarchyLookup<>(BlockEntity.class);
		entityDataProviders = new HierarchyLookup<>(Entity.class);
	}

	@Override
	public void addConfig(ResourceLocation key, boolean defaultValue) {
		if (FMLEnvironment.dist.isClient()) {
			PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, false));
		}
	}

	@Override
	public void addSyncedConfig(ResourceLocation key, boolean defaultValue) {
		PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, true));
	}

	@Override
	public void registerBlockDataProvider(IServerDataProvider<BlockEntity> dataProvider, Class<? extends BlockEntity> block) {
		blockDataProviders.register(block, dataProvider);
	}

	@Override
	public void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity) {
		entityDataProviders.register(entity, dataProvider);
	}

	/* PROVIDER GETTERS */

	public List<IServerDataProvider<BlockEntity>> getBlockNBTProviders(BlockEntity block) {
		return blockDataProviders.get(block);
	}

	public List<IServerDataProvider<Entity>> getEntityNBTProviders(Entity entity) {
		return entityDataProviders.get(entity);
	}

}
