package snownee.jade.impl;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.impl.lookup.IHierarchyLookup;

public class CommonRegistrationSession {
	private final WailaCommonRegistration registration;
	private boolean active;
	private final List<Pair<IServerDataProvider<BlockAccessor>, Class<?>>> blockDataProviders = Lists.newArrayList();
	private final List<Pair<IServerDataProvider<EntityAccessor>, Class<? extends Entity>>> entityDataProviders = Lists.newArrayList();
	private final List<Pair<IServerExtensionProvider<ItemStack>, Class<?>>> itemStorageProviders = Lists.newArrayList();
	private final List<Pair<IServerExtensionProvider<CompoundTag>, Class<?>>> fluidStorageProviders = Lists.newArrayList();
	private final List<Pair<IServerExtensionProvider<CompoundTag>, Class<?>>> energyStorageProviders = Lists.newArrayList();
	private final List<Pair<IServerExtensionProvider<CompoundTag>, Class<?>>> progressProviders = Lists.newArrayList();

	public CommonRegistrationSession(WailaCommonRegistration registration) {
		this.registration = registration;
	}

	private static <T extends IJadeProvider, C> void register(
			T provider,
			List<Pair<T, Class<? extends C>>> list,
			IHierarchyLookup<T> lookup,
			Class<? extends C> clazz) {
		Preconditions.checkArgument(
				lookup.isClassAcceptable(clazz),
				"Class %s is not acceptable",
				clazz);
		Objects.requireNonNull(provider.getUid());
		list.add(Pair.of(provider, clazz));
	}

	public void registerBlockDataProvider(IServerDataProvider<BlockAccessor> dataProvider, Class<?> blockOrBlobkEntityClass) {
		register(dataProvider, blockDataProviders, registration.blockDataProviders, blockOrBlobkEntityClass);
	}

	public void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entityClass) {
		register(dataProvider, entityDataProviders, registration.entityDataProviders, entityClass);
	}

	public <T> void registerEnergyStorage(IServerExtensionProvider<CompoundTag> provider, Class<? extends T> clazz) {
		register(provider, energyStorageProviders, registration.energyStorageProviders, clazz);
	}

	public <T> void registerItemStorage(IServerExtensionProvider<ItemStack> provider, Class<? extends T> clazz) {
		register(provider, itemStorageProviders, registration.itemStorageProviders, clazz);
	}

	public <T> void registerFluidStorage(IServerExtensionProvider<CompoundTag> provider, Class<? extends T> clazz) {
		register(provider, fluidStorageProviders, registration.fluidStorageProviders, clazz);
	}

	public <T> void registerProgress(IServerExtensionProvider<CompoundTag> provider, Class<? extends T> clazz) {
		register(provider, progressProviders, registration.progressProviders, clazz);
	}

	public void reset() {
		blockDataProviders.clear();
		entityDataProviders.clear();
		itemStorageProviders.clear();
		fluidStorageProviders.clear();
		energyStorageProviders.clear();
		progressProviders.clear();
		active = true;
	}

	public void end() {
		Preconditions.checkState(active, "Session is not active");
		active = false;
		blockDataProviders.forEach(pair -> registration.registerBlockDataProvider(pair.getFirst(), pair.getSecond()));
		entityDataProviders.forEach(pair -> registration.registerEntityDataProvider(pair.getFirst(), pair.getSecond()));
		itemStorageProviders.forEach(pair -> registration.registerItemStorage(pair.getFirst(), pair.getSecond()));
		fluidStorageProviders.forEach(pair -> registration.registerFluidStorage(pair.getFirst(), pair.getSecond()));
		energyStorageProviders.forEach(pair -> registration.registerEnergyStorage(pair.getFirst(), pair.getSecond()));
		progressProviders.forEach(pair -> registration.registerProgress(pair.getFirst(), pair.getSecond()));
	}

	public boolean isActive() {
		return active;
	}
}
