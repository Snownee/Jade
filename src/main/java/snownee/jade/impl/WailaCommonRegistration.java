package snownee.jade.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.lookup.HierarchyLookup;
import snownee.jade.impl.lookup.PairHierarchyLookup;
import snownee.jade.impl.lookup.WrappedHierarchyLookup;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	private static volatile WailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	public final PairHierarchyLookup<IServerDataProvider<BlockAccessor>> blockDataProviders;
	public final HierarchyLookup<IServerDataProvider<EntityAccessor>> entityDataProviders;
	public final PriorityStore<ResourceLocation, IJadeProvider> priorities;

	public final WrappedHierarchyLookup<IServerExtensionProvider<ItemStack>> itemStorageProviders;
	public final WrappedHierarchyLookup<IServerExtensionProvider<FluidView.Data>> fluidStorageProviders;
	public final WrappedHierarchyLookup<IServerExtensionProvider<EnergyView.Data>> energyStorageProviders;
	public final WrappedHierarchyLookup<IServerExtensionProvider<ProgressView.Data>> progressProviders;

	WailaCommonRegistration() {
		blockDataProviders = new PairHierarchyLookup<>(new HierarchyLookup<>(Block.class), new HierarchyLookup<>(BlockEntity.class));
		blockDataProviders.idMapped();
		entityDataProviders = new HierarchyLookup<>(Entity.class);
		entityDataProviders.idMapped();
		priorities = new PriorityStore<>(IJadeProvider::getDefaultPriority, IJadeProvider::getUid);
		priorities.setSortingFunction((store, allKeys) -> {
			List<ResourceLocation> keys = allKeys.stream()
					.filter(IPluginConfig::isPrimaryKey)
					.sorted(Comparator.comparingInt(store::byKey))
					.collect(Collectors.toCollection(ArrayList::new));
			allKeys.stream().filter(Predicate.not(IPluginConfig::isPrimaryKey)).forEach($ -> {
				int index = keys.indexOf(IPluginConfig.getPrimaryKey($));
				keys.add(index + 1, $);
			});
			return keys;
		});
		priorities.configurable(Jade.ID + "/sort-order", ResourceLocation.CODEC);

		itemStorageProviders = WrappedHierarchyLookup.forAccessor();
		fluidStorageProviders = WrappedHierarchyLookup.forAccessor();
		energyStorageProviders = WrappedHierarchyLookup.forAccessor();
		progressProviders = WrappedHierarchyLookup.forAccessor();
	}

	public static WailaCommonRegistration instance() {
		if (INSTANCE == null) {
			Jade.LOGGER.error("WailaCommonRegistration is not initialized yet.");
			synchronized (WailaCommonRegistration.class) {
				if (INSTANCE == null) {
					INSTANCE = new WailaCommonRegistration();
				}
			}
		}
		return INSTANCE;
	}

	public static void reset() {
		synchronized (WailaCommonRegistration.class) {
			INSTANCE = new WailaCommonRegistration();
		}
	}

	@Override
	public void registerBlockDataProvider(IServerDataProvider<BlockAccessor> dataProvider, Class<?> blockOrBlobkEntityClass) {
		blockDataProviders.register(blockOrBlobkEntityClass, dataProvider);
	}

	@Override
	public void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entityClass) {
		entityDataProviders.register(entityClass, dataProvider);
	}

	/* PROVIDER GETTERS */

	public List<IServerDataProvider<BlockAccessor>> getBlockNBTProviders(Block block, @Nullable BlockEntity blockEntity) {
		if (blockEntity == null) {
			return blockDataProviders.first.get(block);
		}
		return blockDataProviders.getMerged(block, blockEntity);
	}

	public List<IServerDataProvider<EntityAccessor>> getEntityNBTProviders(Entity entity) {
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
	public <T> void registerItemStorage(IServerExtensionProvider<ItemStack> provider, Class<? extends T> clazz) {
		itemStorageProviders.register(clazz, provider);
	}

	@Override
	public <T> void registerFluidStorage(IServerExtensionProvider<FluidView.Data> provider, Class<? extends T> clazz) {
		fluidStorageProviders.register(clazz, provider);
	}

	@Override
	public <T> void registerEnergyStorage(IServerExtensionProvider<EnergyView.Data> provider, Class<? extends T> clazz) {
		energyStorageProviders.register(clazz, provider);
	}

	@Override
	public <T> void registerProgress(IServerExtensionProvider<ProgressView.Data> provider, Class<? extends T> clazz) {
		progressProviders.register(clazz, provider);
	}
}
