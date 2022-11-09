package snownee.jade.api;

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeRenderBackgroundCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;

@NonExtendable
public interface IWailaClientRegistration {

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 *
	 * @param key          the namespaced key
	 * @param defaultValue the default value
	 */
	void addConfig(ResourceLocation key, boolean defaultValue);

	void addConfig(ResourceLocation key, Enum<?> defaultValue);

	void addConfig(ResourceLocation key, String defaultValue, Predicate<String> validator);

	void addConfig(ResourceLocation key, int defaultValue, int min, int max, boolean slider);

	void addConfig(ResourceLocation key, float defaultValue, float min, float max, boolean slider);

	/**
	 * Register an {@link IJadeProvider} instance to allow overriding the icon for a block via the
	 * {@link IBlockComponentProvider#getIcon(BlockAccessor, IPluginConfig, IElement)} method.
	 *
	 * @param provider The data provider instance
	 * @param block    The highest level class to apply to
	 */
	void registerBlockIcon(IBlockComponentProvider provider, Class<? extends Block> block);

	/**
	 * Register an {@link IJadeProvider} instance for appending informations to
	 * the tooltip.
	 *
	 * @param provider The data provider instance
	 * @param block    The highest level class to apply to
	 */
	void registerBlockComponent(IBlockComponentProvider provider, Class<? extends Block> block);

	/**
	 * Register an {@link IEntityComponentProvider} instance to allow overriding the icon for a entity via the
	 * {@link IEntityComponentProvider#getIcon(EntityAccessor, IPluginConfig, IElement)} method.
	 *
	 * @param provider The data provider instance
	 * @param entity   The highest level class to apply to
	 */
	void registerEntityIcon(IEntityComponentProvider provider, Class<? extends Entity> entity);

	/**
	 * Register an {@link IEntityComponentProvider} instance for appending {@link net.minecraft.network.chat.Component}
	 * to the tooltip.
	 *
	 * @param provider The data provider instance
	 * @param entity   The highest level class to apply to
	 */
	void registerEntityComponent(IEntityComponentProvider provider, Class<? extends Entity> entity);

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

	IElementHelper getElementHelper();

	IDisplayHelper getDisplayHelper();

	IWailaConfig getConfig();

	BlockAccessor.Builder blockAccessor();

	EntityAccessor.Builder entityAccessor();

	boolean shouldHide(Entity target);

	boolean shouldHide(BlockState state);

	boolean shouldPick(BlockState blockState);

	void addAfterRenderCallback(JadeAfterRenderCallback callback);

	void addBeforeRenderCallback(JadeBeforeRenderCallback callback);

	void addRayTraceCallback(JadeRayTraceCallback callback);

	void addTooltipCollectedCallback(JadeTooltipCollectedCallback callback);

	void addItemModNameCallback(JadeItemModNameCallback callback);

	void addRenderBackgroundCallback(JadeRenderBackgroundCallback callback);

	Screen createPluginConfigScreen(@Nullable Screen parent, String namespace);

	@Experimental
	void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider);

	@Experimental
	void registerFluidStorageClient(IClientExtensionProvider<CompoundTag, FluidView> provider);

	@Experimental
	void registerEnergyStorageClient(IClientExtensionProvider<CompoundTag, EnergyView> provider);

	@Experimental
	void registerProgressClient(IClientExtensionProvider<CompoundTag, ProgressView> provider);

}
