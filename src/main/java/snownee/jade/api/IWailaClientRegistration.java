package snownee.jade.api;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeTooltipCollectCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.platform.PlatformWailaClientRegistration;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;

@NonExtendable
public interface IWailaClientRegistration extends PlatformWailaClientRegistration {

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 *
	 * @param key          the namespaced key
	 * @param defaultValue the default value
	 */
	void addConfig(ResourceLocation key, boolean defaultValue);

	<T extends Enum<T>> void addConfig(ResourceLocation key, T defaultValue);

	void addConfig(ResourceLocation key, String defaultValue, Predicate<String> validator);

	void addConfig(ResourceLocation key, int defaultValue, int min, int max, boolean slider);

	void addConfig(ResourceLocation key, float defaultValue, float min, float max, boolean slider);

	void addConfigListener(ResourceLocation key, Consumer<ResourceLocation> listener);

	@ApiStatus.Experimental
	void setConfigCategoryOverride(ResourceLocation key, Component override);

	@ApiStatus.Experimental
	void setConfigCategoryOverride(ResourceLocation key, List<Component> override);

	/**
	 * Register an {@link IJadeProvider} instance to allow overriding the icon for a block via the
	 * {@link IBlockComponentProvider#getIcon(BlockAccessor, IPluginConfig, IElement)} method.
	 *
	 * @param provider   The data provider instance
	 * @param blockClass The highest level class to apply to
	 */
	void registerBlockIcon(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass);

	/**
	 * Register an {@link IJadeProvider} instance for appending informations to
	 * the tooltip.
	 *
	 * @param provider   The data provider instance
	 * @param blockClass The highest level class to apply to
	 */
	void registerBlockComponent(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass);

	/**
	 * Register an {@link IEntityComponentProvider} instance to allow overriding the icon for a entity via the
	 * {@link IEntityComponentProvider#getIcon(EntityAccessor, IPluginConfig, IElement)} method.
	 *
	 * @param provider    The data provider instance
	 * @param entityClass The highest level class to apply to
	 */
	void registerEntityIcon(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass);

	/**
	 * Register an {@link IEntityComponentProvider} instance for appending {@link net.minecraft.network.chat.Component}
	 * to the tooltip.
	 *
	 * @param provider    The data provider instance
	 * @param entityClass The highest level class to apply to
	 */
	void registerEntityComponent(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass);

	/**
	 * Mark a block as hidden in tooltip.
	 */
	void hideTarget(Block block);

	/**
	 * Mark an entity as hidden in tooltip. If player is aiming to this entity, it will be ignored and
	 * try to find next possible target.
	 */
	void hideTarget(EntityType<?> entityType);

	/**
	 * Mark a block to show name of the picked result, rather than block name.
	 */
	void usePickedResult(Block block);

	/**
	 * Mark an entity type to show name of the picked result, rather than entity name.
	 */
	void usePickedResult(EntityType<?> entityType);

	BlockAccessor.Builder blockAccessor();

	EntityAccessor.Builder entityAccessor();

	boolean shouldHide(Entity target);

	boolean shouldHide(BlockState state);

	boolean shouldPick(Entity entity);

	boolean shouldPick(BlockState blockState);

	default void addAfterRenderCallback(JadeAfterRenderCallback callback) {
		addAfterRenderCallback(0, callback);
	}

	Set<ResourceLocation> getConfigKeys(String namespace);

	Set<ResourceLocation> getConfigKeys();

	boolean hasConfig(ResourceLocation key);

	void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback);

	default void addBeforeRenderCallback(JadeBeforeRenderCallback callback) {
		addBeforeRenderCallback(0, callback);
	}

	void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback);

	default void addRayTraceCallback(JadeRayTraceCallback callback) {
		addRayTraceCallback(0, callback);
	}

	void addRayTraceCallback(int priority, JadeRayTraceCallback callback);

	default void addTooltipCollectedCallback(JadeTooltipCollectedCallback callback) {
		addTooltipCollectedCallback(0, callback);
	}

	void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback);

	default void addItemModNameCallback(JadeItemModNameCallback callback) {
		addItemModNameCallback(0, callback);
	}

	void addItemModNameCallback(int priority, JadeItemModNameCallback callback);

	default void addBeforeTooltipCollectCallback(JadeBeforeTooltipCollectCallback callback) {
		addBeforeTooltipCollectCallback(0, callback);
	}

	void addBeforeTooltipCollectCallback(int priority, JadeBeforeTooltipCollectCallback callback);

	Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable Component jumpToCategory);

	void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider);

	void registerFluidStorageClient(IClientExtensionProvider<FluidView.Data, FluidView> provider);

	void registerEnergyStorageClient(IClientExtensionProvider<EnergyView.Data, EnergyView> provider);

	void registerProgressClient(IClientExtensionProvider<ProgressView.Data, ProgressView> provider);

	boolean isServerConnected();

	boolean isShowDetailsPressed();

	boolean maybeLowVisionUser();

	CompoundTag getServerData();

	void setServerData(CompoundTag tag);

	ItemStack getBlockCamouflage(LevelAccessor level, BlockPos pos);

	void markAsClientFeature(ResourceLocation uid);

	void markAsServerFeature(ResourceLocation uid);

	boolean isClientFeature(ResourceLocation uid);

	<T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler);

	AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz);
}
