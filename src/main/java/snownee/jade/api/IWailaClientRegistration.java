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

	@ApiStatus.Experimental
	void setConfigCategoryOverride(Identifier key, Component override);

	@ApiStatus.Experimental
	void setConfigCategoryOverride(Identifier key, List<Component> override);

	/**
	 * Register an {@link IJadeProvider} instance to allow overriding the icon for a block via the
	 * {@link IComponentProvider#getIcon(Accessor, snownee.jade.api.config.IPluginConfig, snownee.jade.api.ui.Element)} method.
	 *
	 * @param provider   The data provider instance
	 * @param blockClass The highest level class to apply to
	 */
	void registerBlockIcon(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass);

	void registerBlockComponent(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass);

	void registerEntityIcon(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass);

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

	default void addAfterRenderCallback(JadeAfterRenderCallback callback) {
		addAfterRenderCallback(0, callback);
	}

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
	 * @param priority callback priority
	 * @param callback render callback
	 */
	void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback);

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

	@Nullable CompoundTag getServerData();

	void setServerData(CompoundTag tag);

	ItemStack getBlockCamouflage(LevelAccessor level, BlockPos pos);

	void markAsClientFeature(Identifier uid);

	void markAsServerFeature(Identifier uid);

	boolean isClientFeature(Identifier uid);

	<T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler);

	AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz);

	void addEntityVariantMapping(EntityType<?> entityType, @Nullable DataComponentType<?> variantType);

	void addVariantType(DataComponentType<?> type, boolean isVariant);

	void reloadIgnoreLists();

	void addHarvestPlugin(Consumer<ToolTypeRegistry> plugin);
}
