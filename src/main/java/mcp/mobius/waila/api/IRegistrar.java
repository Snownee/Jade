package mcp.mobius.waila.api;

import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.block.Block;
import net.minecraft.command.impl.data.IDataAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public interface IRegistrar {

	/**
     * Registers a namespaced config key to be accessed within data providers.
     *
     * @param key the namespaced key
     * @param defaultValue the default value
     */
	void addConfig(ResourceLocation key, boolean defaultValue);

	/**
     * Registers a namespaced config key to be accessed within data providers. These values are sent from the server to
     * the client upon connection.
     *
     * @param key The namespaced key
     * @param defaultValue The default value
     */
	void addSyncedConfig(ResourceLocation key, boolean defaultValue);

	/**
     * Registers an {@link IComponentProvider} instance to allow overriding the displayed item for a block via the
     * {@link IComponentProvider#getIcon(IDataAccessor, IPluginConfig)} method. A {@link TileEntity}
     * is also an acceptable class type.
     *
     * @param dataProvider The data provider instance
     * @param block The highest level class to apply to
     */
	void registerIconProvider(IComponentProvider dataProvider, Class<? extends Block> block);

	/**
     * Registers an {@link IComponentProvider} instance for appending {@link net.minecraft.util.text.ITextComponent} to
     * the tooltip.
     * A {@link TileEntity} is also an acceptable class type.
     *
     * @param dataProvider The data provider instance
     * @param position The position on the tooltip this applies to
     * @param block The highest level class to apply to
     */
	void registerComponentProvider(IComponentProvider dataProvider, TooltipPosition position, Class<? extends Block> block);

	/**
	 * Registers an {@link IServerDataProvider<TileEntity>} instance for data syncing purposes.
	 * A {@link TileEntity} is also an acceptable class type.
	 *
	 * @param dataProvider The data provider instance
	 * @param block The highest level class to apply to
	 */
	void registerBlockDataProvider(IServerDataProvider<TileEntity> dataProvider, Class<? extends TileEntity> block);

	/**
     * Registers an {@link IEntityComponentProvider} instance to allow displaying an item next to the entity name.
     *
     * @param dataProvider The data provider instance
     * @param entity The highest level class to apply to
     */
	void registerIconProvider(IEntityComponentProvider dataProvider, Class<? extends Entity> entity);

	/**
     * Registers an {@link IEntityComponentProvider} instance for appending {@link net.minecraft.util.text.ITextComponent}
     * to the tooltip.
     *
     * @param dataProvider The data provider instance
     * @param position The position on the tooltip this applies to
     * @param entity The highest level class to apply to
     */
	void registerComponentProvider(IEntityComponentProvider dataProvider, TooltipPosition position, Class<? extends Entity> entity);

	void hideTarget(Block block);

	void hideTarget(EntityType<?> entityType);

	void usePickedResult(Block block);

	/**
     * Registers an {@link IEntityComponentProvider} instance for data syncing purposes.
     *
     * @param dataProvider The data provider instance
     * @param entity The highest level class to apply to
     */
	void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity);

	IElementHelper getElementHelper();

	IDisplayHelper getDisplayHelper();

	WailaConfig getConfig();
}
