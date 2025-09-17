package snownee.jade.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.TargetOperationRepository;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.config.TargetOperationRepositoryImpl;
import snownee.jade.impl.lookup.HierarchyLookup;
import snownee.jade.impl.lookup.PairHierarchyLookup;
import snownee.jade.impl.lookup.WrappedHierarchyLookup;
import snownee.jade.util.CommonProxy;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	private static volatile WailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	public final PairHierarchyLookup<IServerDataProvider<BlockAccessor>> blockDataProviders;
	public final HierarchyLookup<IServerDataProvider<EntityAccessor>> entityDataProviders;
	public final PriorityStore<ResourceLocation, IJadeProvider> priorities;

	public final WrappedHierarchyLookup<IServerExtensionProvider<ItemStack>> itemStorageProviders;
	public final WrappedHierarchyLookup<IServerExtensionProvider<FluidView.Data>> fluidStorageProviders;
	public final WrappedHierarchyLookup<IServerExtensionProvider<EnergyView.Data>> energyStorageProviders;
	public final WrappedHierarchyLookup<IServerExtensionProvider<ProgressView.Data>> progressProviders;

	private final TargetOperationRepositoryImpl<Block, BlockState> blockOperations;
	private final TargetOperationRepositoryImpl<EntityType<?>, Entity> entityTypeOperations;
	private final TargetOperationRepositoryImpl<MobEffect, MobEffectInstance> mobEffectOperations;

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

		blockOperations = new TargetOperationRepositoryImpl<>(
				Registries.BLOCK,
				$ -> $.getBlockHolder().unwrapKey().orElseThrow(),
				"hide-blocks",
				() -> CommonProxy.isPhysicallyClient() ? List.of("barrier") : List.of());
		//noinspection deprecation
		entityTypeOperations = new TargetOperationRepositoryImpl<>(
				Registries.ENTITY_TYPE,
				$ -> $.getType().builtInRegistryHolder().key(),
				"hide-entities",
				() -> CommonProxy.isPhysicallyClient() ?
						List.of("area_effect_cloud", "firework_rocket", "interaction", "text_display", "lightning_bolt") :
						List.of());
		mobEffectOperations = new TargetOperationRepositoryImpl<>(
				Registries.MOB_EFFECT,
				$ -> $.getEffect().unwrapKey().orElseThrow(),
				"hide-mob-effects",
				List::of);
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
		checkDataProvider(dataProvider);
		blockDataProviders.register(blockOrBlobkEntityClass, dataProvider);
	}

	@Override
	public void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entityClass) {
		checkDataProvider(dataProvider);
		entityDataProviders.register(entityClass, dataProvider);
	}

	private static void checkDataProvider(IServerDataProvider<?> dataProvider) {
		if (CommonProxy.isPhysicallyClient() && dataProvider instanceof IComponentProvider) {
			throw new IllegalArgumentException(
					"Data providers cannot implement IComponentProvider since Minecraft 1.21.6. Use a separate client provider instead.");
		}
	}

	/* PROVIDER GETTERS */
	public List<IServerDataProvider<BlockAccessor>> blockDataProvidersOf(
			BlockState blockState,
			@Nullable BlockEntity blockEntity,
			boolean checkIsHidden) {
		if (checkIsHidden && blockOperations().shouldHide(blockState)) {
			return List.of();
		}
		if (blockEntity == null) {
			return blockDataProviders.first.get(blockState.getBlock());
		}
		return blockDataProviders.getMerged(blockState.getBlock(), blockEntity);
	}

	public List<IServerDataProvider<EntityAccessor>> entityDataProvidersOf(Entity entity) {
		if (entityTypeOperations().shouldHide(entity)) {
			return List.of();
		}
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

	public void reloadOperations(HolderLookup.Provider provider) {
		blockOperations.reload(provider);
		entityTypeOperations.reload(provider);
		mobEffectOperations.reload(provider);
	}

	@Override
	public TargetOperationRepository<Block, BlockState> blockOperations() {
		return blockOperations;
	}

	@Override
	public TargetOperationRepository<EntityType<?>, Entity> entityTypeOperations() {
		return entityTypeOperations;
	}

	@Override
	public TargetOperationRepository<MobEffect, MobEffectInstance> mobEffectOperations() {
		return mobEffectOperations;
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
