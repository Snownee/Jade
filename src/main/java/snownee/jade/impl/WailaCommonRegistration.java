package snownee.jade.impl;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.view.IServerExtensionProvider;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	public static final WailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	public final HierarchyLookup<IServerDataProvider<BlockEntity>> blockDataProviders;
	public final HierarchyLookup<IServerDataProvider<Entity>> entityDataProviders;
	public final PriorityStore<IJadeProvider> priorities;

	public final HierarchyLookup<IServerExtensionProvider<Object, ItemStack>> itemStorageProviders;
	public final HierarchyLookup<IServerExtensionProvider<Object, CompoundTag>> fluidStorageProviders;
	public final HierarchyLookup<IServerExtensionProvider<Object, CompoundTag>> energyStorageProviders;
	public final HierarchyLookup<IServerExtensionProvider<Object, CompoundTag>> progressProviders;

	WailaCommonRegistration() {
		blockDataProviders = new HierarchyLookup<>(BlockEntity.class);
		entityDataProviders = new HierarchyLookup<>(Entity.class);
		priorities = new PriorityStore<>(Jade.MODID + "/sort-order", IJadeProvider::getDefaultPriority, IJadeProvider::getUid);

		itemStorageProviders = new HierarchyLookup<>(Object.class, true);
		fluidStorageProviders = new HierarchyLookup<>(Object.class, true);
		energyStorageProviders = new HierarchyLookup<>(Object.class, true);
		progressProviders = new HierarchyLookup<>(Object.class, true);
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
		itemStorageProviders.loadComplete(priorities);
		fluidStorageProviders.loadComplete(priorities);
		energyStorageProviders.loadComplete(priorities);
		progressProviders.loadComplete(priorities);
	}

	@Override
	public <T> void registerItemStorage(IServerExtensionProvider<T, ItemStack> provider, Class<? extends T> clazz) {
		itemStorageProviders.register(clazz, (IServerExtensionProvider<Object, ItemStack>) provider);
	}

	@Override
	public <T> void registerFluidStorage(IServerExtensionProvider<T, CompoundTag> provider, Class<? extends T> clazz) {
		fluidStorageProviders.register(clazz, (IServerExtensionProvider<Object, CompoundTag>) provider);
	}

	@Override
	public <T> void registerEnergyStorage(IServerExtensionProvider<T, CompoundTag> provider, Class<? extends T> clazz) {
		energyStorageProviders.register(clazz, (IServerExtensionProvider<Object, CompoundTag>) provider);
	}

	@Override
	public <T> void registerProgress(IServerExtensionProvider<T, CompoundTag> provider, Class<? extends T> clazz) {
		progressProviders.register(clazz, (IServerExtensionProvider<Object, CompoundTag>) provider);
	}

}
