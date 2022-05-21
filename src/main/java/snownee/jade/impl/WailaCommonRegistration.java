package snownee.jade.impl;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	public static final WailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	public final HierarchyLookup<IServerDataProvider<BlockEntity>> blockDataProviders;
	public final HierarchyLookup<IServerDataProvider<Entity>> entityDataProviders;
	public final PriorityStore<IJadeProvider> priorities;

	WailaCommonRegistration() {
		blockDataProviders = new HierarchyLookup<>(BlockEntity.class);
		entityDataProviders = new HierarchyLookup<>(Entity.class);
		priorities = new PriorityStore<>(Jade.MODID + "/sort-order", IJadeProvider::getDefaultPriority);
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

	public void loadComplete() {
		blockDataProviders.loadComplete(priorities);
		entityDataProviders.loadComplete(priorities);
	}

}
