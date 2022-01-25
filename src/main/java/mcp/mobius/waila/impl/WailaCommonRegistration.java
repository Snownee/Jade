package mcp.mobius.waila.impl;

import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	public static final IWailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	@Override
	public void addConfig(ResourceLocation key, boolean defaultValue) {
		WailaRegistrar.INSTANCE.addConfig(key, defaultValue);
	}

	@Override
	public void addSyncedConfig(ResourceLocation key, boolean defaultValue) {
		WailaRegistrar.INSTANCE.addSyncedConfig(key, defaultValue);
	}

	@Override
	public void registerBlockDataProvider(IServerDataProvider<BlockEntity> dataProvider, Class<? extends BlockEntity> block) {
		WailaRegistrar.INSTANCE.registerBlockDataProvider(dataProvider, block);
	}

	@Override
	public void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity) {
		WailaRegistrar.INSTANCE.registerEntityDataProvider(dataProvider, entity);
	}

}
