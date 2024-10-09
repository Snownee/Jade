package snownee.jade.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeTooltipCollectCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.entry.BooleanConfigEntry;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.impl.config.entry.EnumConfigEntry;
import snownee.jade.impl.config.entry.FloatConfigEntry;
import snownee.jade.impl.config.entry.IntConfigEntry;
import snownee.jade.impl.config.entry.StringConfigEntry;
import snownee.jade.impl.lookup.IHierarchyLookup;

public class ClientRegistrationSession {
	private final WailaClientRegistration registration;
	private boolean active;
	private final List<Pair<IComponentProvider<BlockAccessor>, Class<? extends Block>>> blockIconProviders = Lists.newArrayList();
	private final List<Pair<IComponentProvider<BlockAccessor>, Class<? extends Block>>> blockComponentProviders = Lists.newArrayList();
	private final List<Pair<IComponentProvider<EntityAccessor>, Class<? extends Entity>>> entityIconProviders = Lists.newArrayList();
	private final List<Pair<IComponentProvider<EntityAccessor>, Class<? extends Entity>>> entityComponentProviders = Lists.newArrayList();
	private final List<ConfigEntry<?>> configEntries = Lists.newArrayList();
	private final Set<ResourceLocation> configIds = Sets.newHashSet();
	private final List<Pair<ResourceLocation, Consumer<ResourceLocation>>> configListeners = Lists.newArrayList();
	private final List<Pair<ResourceLocation, Component>> configCategoryOverrides = Lists.newArrayList();
	private final List<IClientExtensionProvider<ItemStack, ItemView>> itemStorageProviders = Lists.newArrayList();
	private final List<IClientExtensionProvider<CompoundTag, FluidView>> fluidStorageProviders = Lists.newArrayList();
	private final List<IClientExtensionProvider<CompoundTag, EnergyView>> energyStorageProviders = Lists.newArrayList();
	private final List<IClientExtensionProvider<CompoundTag, ProgressView>> progressProviders = Lists.newArrayList();
	private final List<Pair<Integer, JadeAfterRenderCallback>> afterRenderCallback = Lists.newArrayList();
	private final List<Pair<Integer, JadeBeforeRenderCallback>> beforeRenderCallback = Lists.newArrayList();
	private final List<Pair<Integer, JadeRayTraceCallback>> rayTraceCallback = Lists.newArrayList();
	private final List<Pair<Integer, JadeTooltipCollectedCallback>> tooltipCollectedCallback = Lists.newArrayList();
	private final List<Pair<Integer, JadeItemModNameCallback>> itemModNameCallback = Lists.newArrayList();
	private final List<Pair<Integer, JadeBeforeTooltipCollectCallback>> beforeTooltipCollectCallback = Lists.newArrayList();

	public ClientRegistrationSession(WailaClientRegistration registration) {
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

	public void registerBlockIcon(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass) {
		register(provider, blockIconProviders, registration.blockIconProviders, blockClass);
		tryAddConfig(provider);
	}

	public void registerBlockComponent(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass) {
		register(provider, blockComponentProviders, registration.blockComponentProviders, blockClass);
		tryAddConfig(provider);
	}

	public void registerEntityIcon(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass) {
		register(provider, entityIconProviders, registration.entityIconProviders, entityClass);
		tryAddConfig(provider);
	}

	public void registerEntityComponent(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass) {
		register(provider, entityComponentProviders, registration.entityComponentProviders, entityClass);
		tryAddConfig(provider);
	}

	public void addConfig(ResourceLocation key, boolean defaultValue) {
		configEntries.add(new BooleanConfigEntry(key, defaultValue));
		configIds.add(key);
	}

	public <T extends Enum<T>> void addConfig(ResourceLocation key, T defaultValue) {
		configEntries.add(new EnumConfigEntry<>(key, defaultValue));
		configIds.add(key);
	}

	public void addConfig(ResourceLocation key, String defaultValue, Predicate<String> validator) {
		configEntries.add(new StringConfigEntry(key, defaultValue, validator));
		configIds.add(key);
	}

	public void addConfig(ResourceLocation key, int defaultValue, int min, int max, boolean slider) {
		configEntries.add(new IntConfigEntry(key, defaultValue, min, max, slider));
		configIds.add(key);
	}

	public void addConfig(ResourceLocation key, float defaultValue, float min, float max, boolean slider) {
		configEntries.add(new FloatConfigEntry(key, defaultValue, min, max, slider));
		configIds.add(key);
	}

	private void tryAddConfig(IToggleableProvider provider) {
		ResourceLocation key = provider.getUid();
		if (!provider.isRequired()) {
			configIds.add(key);
		}
	}

	public void addConfigListener(ResourceLocation key, Consumer<ResourceLocation> listener) {
		configListeners.add(Pair.of(key, listener));
	}

	public void setConfigCategoryOverride(ResourceLocation key, Component override) {
		Preconditions.checkNotNull(override, "Override cannot be null");
		Preconditions.checkArgument(configIds.contains(key) || registration.hasConfig(key), "Unknown config key: %s", key);
		Preconditions.checkArgument(PluginConfig.isPrimaryKey(key), "Only primary config key can be overridden");
		configCategoryOverrides.add(Pair.of(key, override));
	}

	public void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider) {
		itemStorageProviders.add(provider);
	}

	public void registerFluidStorageClient(IClientExtensionProvider<CompoundTag, FluidView> provider) {
		fluidStorageProviders.add(provider);
	}

	public void registerEnergyStorageClient(IClientExtensionProvider<CompoundTag, EnergyView> provider) {
		energyStorageProviders.add(provider);
	}

	public void registerProgressClient(IClientExtensionProvider<CompoundTag, ProgressView> provider) {
		progressProviders.add(provider);
	}

	public void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback) {
		afterRenderCallback.add(Pair.of(priority, callback));
	}

	public void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback) {
		beforeRenderCallback.add(Pair.of(priority, callback));
	}

	public void addRayTraceCallback(int priority, JadeRayTraceCallback callback) {
		rayTraceCallback.add(Pair.of(priority, callback));
	}

	public void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback) {
		tooltipCollectedCallback.add(Pair.of(priority, callback));
	}

	public void addItemModNameCallback(int priority, JadeItemModNameCallback callback) {
		itemModNameCallback.add(Pair.of(priority, callback));
	}

	public void addBeforeTooltipCollectCallback(int priority, JadeBeforeTooltipCollectCallback callback) {
		beforeTooltipCollectCallback.add(Pair.of(priority, callback));
	}

	public void reset() {
		blockIconProviders.clear();
		blockComponentProviders.clear();
		entityIconProviders.clear();
		entityComponentProviders.clear();
		configEntries.clear();
		configIds.clear();
		configListeners.clear();
		configCategoryOverrides.clear();
		itemStorageProviders.clear();
		fluidStorageProviders.clear();
		energyStorageProviders.clear();
		progressProviders.clear();
		afterRenderCallback.clear();
		beforeRenderCallback.clear();
		rayTraceCallback.clear();
		tooltipCollectedCallback.clear();
		itemModNameCallback.clear();
		beforeTooltipCollectCallback.clear();
		active = true;
	}

	public void end() {
		Preconditions.checkState(active, "Session is not active");
		active = false;
		blockIconProviders.forEach(pair -> registration.registerBlockIcon(pair.getFirst(), pair.getSecond()));
		blockComponentProviders.forEach(pair -> registration.registerBlockComponent(pair.getFirst(), pair.getSecond()));
		entityIconProviders.forEach(pair -> registration.registerEntityIcon(pair.getFirst(), pair.getSecond()));
		entityComponentProviders.forEach(pair -> registration.registerEntityComponent(pair.getFirst(), pair.getSecond()));
		configEntries.forEach(registration::addConfig);
		configListeners.forEach(pair -> registration.addConfigListener(pair.getFirst(), pair.getSecond()));
		configCategoryOverrides.forEach(pair -> registration.setConfigCategoryOverride(pair.getFirst(), pair.getSecond()));
		itemStorageProviders.forEach(registration::registerItemStorageClient);
		fluidStorageProviders.forEach(registration::registerFluidStorageClient);
		energyStorageProviders.forEach(registration::registerEnergyStorageClient);
		progressProviders.forEach(registration::registerProgressClient);
		afterRenderCallback.forEach(pair -> registration.addAfterRenderCallback(pair.getFirst(), pair.getSecond()));
		beforeRenderCallback.forEach(pair -> registration.addBeforeRenderCallback(pair.getFirst(), pair.getSecond()));
		rayTraceCallback.forEach(pair -> registration.addRayTraceCallback(pair.getFirst(), pair.getSecond()));
		tooltipCollectedCallback.forEach(pair -> registration.addTooltipCollectedCallback(pair.getFirst(), pair.getSecond()));
		itemModNameCallback.forEach(pair -> registration.addItemModNameCallback(pair.getFirst(), pair.getSecond()));
		beforeTooltipCollectCallback.forEach(pair -> registration.addBeforeTooltipCollectCallback(pair.getFirst(), pair.getSecond()));
	}

	public boolean isActive() {
		return active;
	}
}
