package snownee.jade.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeRenderBackgroundCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.platform.CustomEnchantPower;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;
import snownee.jade.gui.PluginsConfigScreen;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.entry.BooleanConfigEntry;
import snownee.jade.impl.config.entry.EnumConfigEntry;
import snownee.jade.impl.config.entry.FloatConfigEntry;
import snownee.jade.impl.config.entry.IntConfigEntry;
import snownee.jade.impl.config.entry.StringConfigEntry;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.ClientPlatformProxy;

public class WailaClientRegistration implements IWailaClientRegistration {

	public static final WailaClientRegistration INSTANCE = new WailaClientRegistration();

	public final HierarchyLookup<IBlockComponentProvider> blockIconProviders;
	public final HierarchyLookup<IBlockComponentProvider> blockComponentProviders;

	public final HierarchyLookup<IEntityComponentProvider> entityIconProviders;
	public final HierarchyLookup<IEntityComponentProvider> entityComponentProviders;

	public final Set<Block> hideBlocks = Sets.newHashSet();
	public final Set<EntityType<?>> hideEntities = Sets.newHashSet();
	public final Set<Block> pickBlocks = Sets.newHashSet();
	public final Set<EntityType<?>> pickEntities = Sets.newHashSet();

	public final CallbackContainer<JadeAfterRenderCallback> afterRenderCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeBeforeRenderCallback> beforeRenderCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeRayTraceCallback> rayTraceCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeTooltipCollectedCallback> tooltipCollectedCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeItemModNameCallback> itemModNameCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeRenderBackgroundCallback> renderBackgroundCallback = new CallbackContainer<>();

	public final Map<Block, CustomEnchantPower> customEnchantPowers = Maps.newHashMap();
	public final Map<ResourceLocation, IClientExtensionProvider<ItemStack, ItemView>> itemStorageProviders = Maps.newHashMap();
	public final Map<ResourceLocation, IClientExtensionProvider<CompoundTag, FluidView>> fluidStorageProviders = Maps.newHashMap();
	public final Map<ResourceLocation, IClientExtensionProvider<CompoundTag, EnergyView>> energyStorageProviders = Maps.newHashMap();
	public final Map<ResourceLocation, IClientExtensionProvider<CompoundTag, ProgressView>> progressProviders = Maps.newHashMap();

	public final Set<ResourceLocation> clientFeatures = Sets.newHashSet();

	WailaClientRegistration() {
		blockIconProviders = new HierarchyLookup<>(Block.class);
		blockComponentProviders = new HierarchyLookup<>(Block.class);

		entityIconProviders = new HierarchyLookup<>(Entity.class);
		entityComponentProviders = new HierarchyLookup<>(Entity.class);
	}

	@Override
	public void registerBlockIcon(IBlockComponentProvider provider, Class<? extends Block> block) {
		blockIconProviders.register(block, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerBlockComponent(IBlockComponentProvider provider, Class<? extends Block> block) {
		blockComponentProviders.register(block, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerEntityIcon(IEntityComponentProvider provider, Class<? extends Entity> entity) {
		entityIconProviders.register(entity, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerEntityComponent(IEntityComponentProvider provider, Class<? extends Entity> entity) {
		entityComponentProviders.register(entity, provider);
		tryAddConfig(provider);
	}

	public List<IBlockComponentProvider> getBlockProviders(Block block, Predicate<IBlockComponentProvider> filter) {
		return blockComponentProviders.get(block).stream().filter(filter).toList();
	}

	public List<IBlockComponentProvider> getBlockIconProviders(Block block, Predicate<IBlockComponentProvider> filter) {
		return blockIconProviders.get(block).stream().filter(filter).toList();
	}

	public List<IEntityComponentProvider> getEntityProviders(Entity entity, Predicate<IEntityComponentProvider> filter) {
		return entityComponentProviders.get(entity).stream().filter(filter).toList();
	}

	public List<IEntityComponentProvider> getEntityIconProviders(Entity entity, Predicate<IEntityComponentProvider> filter) {
		return entityIconProviders.get(entity).stream().filter(filter).toList();
	}

	@Override
	public void hideTarget(Block block) {
		Objects.requireNonNull(block);
		hideBlocks.add(block);
	}

	@Override
	public void hideTarget(EntityType<?> entityType) {
		Objects.requireNonNull(entityType);
		hideEntities.add(entityType);
	}

	@Override
	public void usePickedResult(Block block) {
		Objects.requireNonNull(block);
		pickBlocks.add(block);
	}

	@Override
	public void usePickedResult(EntityType<?> entityType) {
		Objects.requireNonNull(entityType);
		pickEntities.add(entityType);
	}

	@Override
	public boolean shouldHide(BlockState state) {
		return hideBlocks.contains(state.getBlock());
	}

	@Override
	public boolean shouldPick(BlockState state) {
		return pickBlocks.contains(state.getBlock());
	}

	@Override
	public boolean shouldHide(Entity entity) {
		return hideEntities.contains(entity.getType());
	}

	@Override
	public boolean shouldPick(Entity entity) {
		return pickEntities.contains(entity.getType());
	}

	@Override
	public IElementHelper getElementHelper() {
		return ElementHelper.INSTANCE;
	}

	@Override
	public IDisplayHelper getDisplayHelper() {
		return DisplayHelper.INSTANCE;
	}

	@Override
	public IWailaConfig getConfig() {
		return Jade.CONFIG.get();
	}

	@Override
	public void addConfig(ResourceLocation key, boolean defaultValue) {
		PluginConfig.INSTANCE.addConfig(new BooleanConfigEntry(key, defaultValue));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addConfig(ResourceLocation key, Enum<?> defaultValue) {
		Objects.requireNonNull(defaultValue);
		PluginConfig.INSTANCE.addConfig(new EnumConfigEntry(key, defaultValue));
	}

	@Override
	public void addConfig(ResourceLocation key, String defaultValue, Predicate<String> validator) {
		Objects.requireNonNull(defaultValue);
		Objects.requireNonNull(validator);
		PluginConfig.INSTANCE.addConfig(new StringConfigEntry(key, defaultValue, validator));
	}

	@Override
	public void addConfig(ResourceLocation key, int defaultValue, int min, int max, boolean slider) {
		PluginConfig.INSTANCE.addConfig(new IntConfigEntry(key, defaultValue, min, max, slider));
	}

	@Override
	public void addConfig(ResourceLocation key, float defaultValue, float min, float max, boolean slider) {
		PluginConfig.INSTANCE.addConfig(new FloatConfigEntry(key, defaultValue, min, max, slider));
	}

	@Override
	public void addConfigListener(ResourceLocation key, Consumer<ResourceLocation> listener) {
		PluginConfig.INSTANCE.addConfigListener(key, listener);
	}

	private void tryAddConfig(IToggleableProvider provider) {
		if (!provider.isRequired() && !PluginConfig.INSTANCE.containsKey(provider.getUid())) {
			addConfig(provider.getUid(), provider.enabledByDefault());
		}
	}

	public void loadComplete() {
		var priorities = WailaCommonRegistration.INSTANCE.priorities;
		blockComponentProviders.loadComplete(priorities);
		blockIconProviders.loadComplete(priorities);
		entityComponentProviders.loadComplete(priorities);
		entityIconProviders.loadComplete(priorities);
		Stream.of(afterRenderCallback, beforeRenderCallback, rayTraceCallback, tooltipCollectedCallback, itemModNameCallback, renderBackgroundCallback).forEach(CallbackContainer::sort);
	}

	@Override
	public void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback) {
		afterRenderCallback.add(priority, callback);
	}

	@Override
	public void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback) {
		beforeRenderCallback.add(priority, callback);
	}

	@Override
	public void addRayTraceCallback(int priority, JadeRayTraceCallback callback) {
		rayTraceCallback.add(priority, callback);
	}

	@Override
	public void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback) {
		tooltipCollectedCallback.add(priority, callback);
	}

	@Override
	public void addItemModNameCallback(int priority, JadeItemModNameCallback callback) {
		itemModNameCallback.add(priority, callback);
	}

	@Override
	public void addRenderBackgroundCallback(int priority, JadeRenderBackgroundCallback callback) {
		renderBackgroundCallback.add(priority, callback);
	}

	@Override
	public BlockAccessor.Builder blockAccessor() {
		Minecraft mc = Minecraft.getInstance();
		/* off */
		return new BlockAccessorImpl.Builder()
				.level(mc.level)
				.player(mc.player)
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
		/* on */
	}

	@Override
	public EntityAccessor.Builder entityAccessor() {
		Minecraft mc = Minecraft.getInstance();
		/* off */
		return new EntityAccessorImpl.Builder()
				.level(mc.level)
				.player(mc.player)
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
		/* on */
	}

	@Override
	public void registerCustomEnchantPower(Block block, CustomEnchantPower customEnchantPower) {
		customEnchantPowers.put(block, customEnchantPower);
	}

	@Override
	public Screen createPluginConfigScreen(@Nullable Screen parent, String namespace) {
		return PluginsConfigScreen.createPluginConfigScreen(parent, namespace, false);
	}

	@Override
	public void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider) {
		Objects.requireNonNull(provider.getUid());
		itemStorageProviders.put(provider.getUid(), provider);
	}

	@Override
	public void registerFluidStorageClient(IClientExtensionProvider<CompoundTag, FluidView> provider) {
		Objects.requireNonNull(provider.getUid());
		fluidStorageProviders.put(provider.getUid(), provider);
	}

	@Override
	public void registerEnergyStorageClient(IClientExtensionProvider<CompoundTag, EnergyView> provider) {
		Objects.requireNonNull(provider.getUid());
		energyStorageProviders.put(provider.getUid(), provider);
	}

	@Override
	public void registerProgressClient(IClientExtensionProvider<CompoundTag, ProgressView> provider) {
		Objects.requireNonNull(provider.getUid());
		progressProviders.put(provider.getUid(), provider);
	}

	@Override
	public boolean isServerConnected() {
		return ObjectDataCenter.serverConnected;
	}

	@Override
	public boolean isShowDetailsPressed() {
		return ClientPlatformProxy.isShowDetailsPressed();
	}

	@Override
	public void setServerData(CompoundTag tag) {
		ObjectDataCenter.setServerData(tag);
	}

	@Override
	public CompoundTag getServerData() {
		return ObjectDataCenter.getServerData();
	}

	@Override
	public ItemStack getBlockCamouflage(LevelAccessor level, BlockPos pos) {
		return DatapackBlockManager.getFakeBlock(level, pos);
	}

	@Override
	public void markAsClientFeature(ResourceLocation uid) {
		clientFeatures.add(uid);
	}

	@Override
	public void markAsServerFeature(ResourceLocation uid) {
		clientFeatures.remove(uid);
	}

	@Override
	public boolean isClientFeature(ResourceLocation uid) {
		return clientFeatures.contains(uid);
	}

}
