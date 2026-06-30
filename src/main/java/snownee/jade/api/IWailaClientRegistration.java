package snownee.jade.api;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeTooltipCollectCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.harvest.ToolTypeRegistry;
import snownee.jade.api.platform.PlatformWailaClientRegistration;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;

/**
 * Client-side registration entry point for Jade integrations.
 */
@NonExtendable
public interface IWailaClientRegistration extends PlatformWailaClientRegistration {

	/**
	 * Registers a boolean plugin config key.
	 *
	 * @param key          configuration key
	 * @param defaultValue default value
	 */
	void addConfig(Identifier key, boolean defaultValue);

	/**
	 * Registers an enum plugin config key.
	 *
	 * @param key          configuration key
	 * @param defaultValue default value
	 * @param <T>          enum type
	 */
	<T extends Enum<T>> void addConfig(Identifier key, T defaultValue);

	/**
	 * Registers a string plugin config key.
	 *
	 * @param key          configuration key
	 * @param defaultValue default value
	 * @param validator    value validator
	 */
	void addConfig(Identifier key, String defaultValue, Predicate<String> validator);

	/**
	 * Registers an integer plugin config key.
	 *
	 * @param key          configuration key
	 * @param defaultValue default value
	 * @param min          minimum accepted value
	 * @param max          maximum accepted value
	 * @param slider       whether the UI should render a slider
	 */
	void addConfig(Identifier key, int defaultValue, int min, int max, boolean slider);

	/**
	 * Registers a floating-point plugin config key.
	 *
	 * @param key          configuration key
	 * @param defaultValue default value
	 * @param min          minimum accepted value
	 * @param max          maximum accepted value
	 * @param slider       whether the UI should render a slider
	 */
	void addConfig(Identifier key, float defaultValue, float min, float max, boolean slider);

	/**
	 * Registers a listener for config changes.
	 *
	 * @param key      configuration key
	 * @param listener callback invoked when the key changes
	 */
	void addConfigListener(Identifier key, Consumer<Identifier> listener);

	/**
	 * Overrides the display category for a config key.
	 *
	 * @param key      config key
	 * @param override override component
	 */
	@ApiStatus.Experimental
	void setConfigCategoryOverride(Identifier key, Component override);

	/**
	 * Overrides the display category for a config key.
	 *
	 * @param key      config key
	 * @param override override components
	 */
	@ApiStatus.Experimental
	void setConfigCategoryOverride(Identifier key, List<Component> override);

	/**
	 * Register an {@link IComponentProvider} instance to allow overriding the icon for a block via the
	 * {@link IComponentProvider#getIcon(Accessor, snownee.jade.api.config.IPluginConfig, snownee.jade.api.ui.Element)} method.
	 *
	 * @param provider   The data provider instance
	 * @param blockClass The highest level class to apply to
	 */
	void registerBlockIcon(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass);

	/**
	 * Registers a block component provider.
	 *
	 * @param provider   component provider
	 * @param blockClass highest-level block class to apply to
	 */
	void registerBlockComponent(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass);

	/**
	 * Registers an {@link IComponentProvider} instance to allow overriding the icon for an entity via the
	 * {@link IComponentProvider#getIcon(Accessor, snownee.jade.api.config.IPluginConfig, snownee.jade.api.ui.Element)} method.
	 *
	 * @param provider    component provider
	 * @param entityClass highest-level entity class to apply to
	 */
	void registerEntityIcon(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass);

	/**
	 * Registers an entity component provider.
	 *
	 * @param provider    component provider
	 * @param entityClass highest-level entity class to apply to
	 */
	void registerEntityComponent(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass);

	/**
	 * Creates an empty accessor builder.
	 *
	 * @return empty accessor builder
	 */
	EmptyAccessor.Builder emptyAccessor();

	/**
	 * Creates a block accessor builder.
	 *
	 * @return block accessor builder
	 */
	BlockAccessor.Builder blockAccessor();

	/**
	 * Creates an entity accessor builder.
	 *
	 * @return entity accessor builder
	 */
	EntityAccessor.Builder entityAccessor();

	/**
	 * Returns the known config keys in a namespace.
	 *
	 * @param namespace namespace to query
	 * @return registered keys
	 */
	Set<Identifier> getConfigKeys(String namespace);

	/**
	 * Returns every registered config key.
	 *
	 * @return all config keys
	 */
	Set<Identifier> getConfigKeys();

	/**
	 * Returns whether a config key exists.
	 *
	 * @param key configuration key
	 * @return {@code true} if the key is registered
	 */
	boolean hasConfig(Identifier key);

	/**
	 * Registers a callback that runs after the overlay renders.
	 *
	 * @param callback render callback
	 */
	default void addAfterRenderCallback(JadeAfterRenderCallback callback) {
		addAfterRenderCallback(0, callback);
	}

	/**
	 * Registers a callback that runs after the overlay renders.
	 *
	 * @param priority callback priority
	 * @param callback render callback
	 */
	void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback);

	/**
	 * Registers a callback that runs before the overlay renders.
	 *
	 * @param callback render callback
	 */
	default void addBeforeRenderCallback(JadeBeforeRenderCallback callback) {
		addBeforeRenderCallback(0, callback);
	}

	/**
	 * Registers a callback that runs before the overlay renders.
	 *
	 * @param priority callback priority
	 * @param callback render callback
	 */
	void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback);

	/**
	 * Registers a callback that runs during ray tracing.
	 *
	 * @param callback ray trace callback
	 */
	default void addRayTraceCallback(JadeRayTraceCallback callback) {
		addRayTraceCallback(0, callback);
	}

	/**
	 * Registers a callback that runs during ray tracing.
	 *
	 * @param priority callback priority
	 * @param callback ray trace callback
	 */
	void addRayTraceCallback(int priority, JadeRayTraceCallback callback);

	/**
	 * Registers a callback that runs after a tooltip is collected.
	 *
	 * @param callback tooltip callback
	 */
	default void addTooltipCollectedCallback(JadeTooltipCollectedCallback callback) {
		addTooltipCollectedCallback(0, callback);
	}

	/**
	 * Registers a callback that runs after a tooltip is collected.
	 *
	 * @param priority callback priority
	 * @param callback tooltip callback
	 */
	void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback);

	/**
	 * Registers a callback that runs when an item mod name is resolved.
	 *
	 * @param callback mod name callback
	 */
	default void addItemModNameCallback(JadeItemModNameCallback callback) {
		addItemModNameCallback(0, callback);
	}

	/**
	 * Registers a callback that runs when an item mod name is resolved.
	 *
	 * @param priority callback priority
	 * @param callback mod name callback
	 */
	void addItemModNameCallback(int priority, JadeItemModNameCallback callback);

	/**
	 * Registers a callback that runs before tooltip collection.
	 *
	 * @param callback tooltip collection callback
	 */
	default void addBeforeTooltipCollectCallback(JadeBeforeTooltipCollectCallback callback) {
		addBeforeTooltipCollectCallback(0, callback);
	}

	/**
	 * Registers a callback that runs before tooltip collection.
	 *
	 * @param priority callback priority
	 * @param callback tooltip collection callback
	 */
	void addBeforeTooltipCollectCallback(int priority, JadeBeforeTooltipCollectCallback callback);

	/**
	 * Creates the plugin configuration screen.
	 *
	 * @param parent         parent screen
	 * @param jumpToCategory optional category to focus
	 * @return configuration screen
	 */
	Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable Component jumpToCategory);

	/**
	 * Registers a client item storage provider.
	 *
	 * @param provider storage provider
	 */
	void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider);

	/**
	 * Registers a client fluid storage provider.
	 *
	 * @param provider storage provider
	 */
	void registerFluidStorageClient(IClientExtensionProvider<FluidView.Data, FluidView> provider);

	/**
	 * Registers a client energy storage provider.
	 *
	 * @param provider storage provider
	 */
	void registerEnergyStorageClient(IClientExtensionProvider<EnergyView.Data, EnergyView> provider);

	/**
	 * Registers a client progress provider.
	 *
	 * @param provider storage provider
	 */
	void registerProgressClient(IClientExtensionProvider<ProgressView.Data, ProgressView> provider);

	/**
	 * Returns whether Jade is connected to a server.
	 *
	 * @return {@code true} if connected to a server
	 */
	boolean isServerConnected();

	/**
	 * Returns whether the details key is pressed.
	 *
	 * @return {@code true} if details are requested
	 */
	boolean isShowDetailsPressed();

	/**
	 * Returns whether the current user is marked as low vision.
	 *
	 * @return {@code true} if low vision mode is enabled
	 */
	boolean maybeLowVisionUser();

	/**
	 * Returns the latest server data payload.
	 *
	 * @return server data, or {@code null} if unavailable
	 */
	@Nullable CompoundTag getServerData();

	/**
	 * Replaces the cached server data payload.
	 *
	 * @param tag server data
	 */
	void setServerData(CompoundTag tag);

	/**
	 * Returns the camouflage item stack for a block at the given position.
	 *
	 * @param level level to query
	 * @param pos   block position
	 * @return camouflage item stack
	 */
	ItemStack getBlockCamouflage(LevelAccessor level, BlockPos pos);

	/**
	 * Marks a feature as client-side, meaning it is fully available even if the server does not have Jade installed.
	 *
	 * @param uid feature identifier
	 */
	void markAsClientFeature(Identifier uid);

	/**
	 * Marks a feature as server-side.
	 *
	 * @param uid feature identifier
	 */
	void markAsServerFeature(Identifier uid);

	/**
	 * Returns whether a feature is fully available even if the server does not have Jade installed.
	 *
	 * @param uid feature identifier
	 * @return {@code true} if the feature is client-side
	 */
	boolean isClientFeature(Identifier uid);

	/**
	 * Registers a client handler for the given accessor type.
	 *
	 * @param clazz   accessor class
	 * @param handler client handler
	 * @param <T>     accessor type
	 */
	<T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler);

	/**
	 * Returns the client handler for the given accessor type.
	 *
	 * @param clazz accessor class
	 * @return accessor handler
	 */
	AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz);

	/**
	 * Registers a variant mapping for an entity type.
	 *
	 * @param entityType  entity type
	 * @param variantType optional variant component type
	 */
	void addEntityVariantMapping(EntityType<?> entityType, @Nullable DataComponentType<?> variantType);

	/**
	 * Marks a data component type as a variant or non-variant type.
	 *
	 * @param type      component type
	 * @param isVariant whether the type is a variant
	 */
	void addVariantType(DataComponentType<?> type, boolean isVariant);

	/**
	 * Reloads Jade ignore lists.
	 */
	void reloadIgnoreLists();

	/**
	 * Registers a harvest plugin.
	 *
	 * @param plugin harvest plugin callback
	 */
	void addHarvestPlugin(Consumer<ToolTypeRegistry> plugin);
}
