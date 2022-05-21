package snownee.jade.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor.Builder;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.config.ConfigEntry;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.overlay.DisplayHelper;

public class WailaClientRegistration implements IWailaClientRegistration {

	public static final WailaClientRegistration INSTANCE = new WailaClientRegistration();

	public final HierarchyLookup<IBlockComponentProvider> blockIconProviders;
	public final HierarchyLookup<IBlockComponentProvider> blockComponentProviders;

	public final HierarchyLookup<IEntityComponentProvider> entityIconProviders;
	public final HierarchyLookup<IEntityComponentProvider> entityComponentProviders;

	public final Set<Block> hideBlocks = Sets.newHashSet();
	public final Set<EntityType<?>> hideEntities = Sets.newHashSet();
	public final Set<Block> pickBlocks = Sets.newHashSet();

	public final List<JadeAfterRenderCallback> afterRenderCallbacks = Lists.newArrayList();
	public final List<JadeBeforeRenderCallback> beforeRenderCallbacks = Lists.newArrayList();
	public final List<JadeRayTraceCallback> rayTraceCallbacks = Lists.newArrayList();
	public final List<JadeTooltipCollectedCallback> tooltipCollectedCallbacks = Lists.newArrayList();

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
		hideBlocks.add(block);
	}

	@Override
	public void hideTarget(EntityType<?> entityType) {
		hideEntities.add(entityType);
	}

	@Override
	public void usePickedResult(Block block) {
		pickBlocks.add(block);
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
		if (!PluginConfig.INSTANCE.getKeys().contains(key)) {
			PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, false));
		}
	}

	private void tryAddConfig(IToggleableProvider provider) {
		if (!provider.isRequired()) {
			addConfig(provider.getUid(), provider.enabledByDefault());
		}
	}

	public void loadComplete() {
		var priorities = WailaCommonRegistration.INSTANCE.priorities;
		blockComponentProviders.loadComplete(priorities);
		blockIconProviders.loadComplete(priorities);
		entityComponentProviders.loadComplete(priorities);
		entityIconProviders.loadComplete(priorities);
	}

	@Override
	public void addAfterRenderCallback(JadeAfterRenderCallback callback) {
		afterRenderCallbacks.add(callback);
	}

	@Override
	public void addBeforeRenderCallback(JadeBeforeRenderCallback callback) {
		beforeRenderCallbacks.add(callback);
	}

	@Override
	public void addRayTraceCallback(JadeRayTraceCallback callback) {
		rayTraceCallbacks.add(callback);
	}

	@Override
	public void addTooltipCollectedCallback(JadeTooltipCollectedCallback callback) {
		tooltipCollectedCallbacks.add(callback);
	}

	@Override
	public Builder blockAccessor() {
		return new BlockAccessorImpl.Builder();
	}

	@Override
	public snownee.jade.api.EntityAccessor.Builder entityAccessor() {
		return new EntityAccessorImpl.Builder();
	}

}
