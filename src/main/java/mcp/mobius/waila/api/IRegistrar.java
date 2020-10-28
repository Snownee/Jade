package mcp.mobius.waila.api;

import net.minecraft.entity.Entity;
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
     * {@link IComponentProvider#getStack(IDataAccessor, IPluginConfig)} method. A {@link TileEntity}
     * is also an acceptable class type.
     *
     * @param dataProvider The data provider instance
     * @param block The highest level class to apply to
     */
    void registerStackProvider(IComponentProvider dataProvider, Class block);

    /**
     * Registers an {@link IComponentProvider} instance for appending {@link net.minecraft.util.text.ITextComponent} to
     * the tooltip.
     * A {@link TileEntity} is also an acceptable class type.
     *
     * @param dataProvider The data provider instance
     * @param position The position on the tooltip this applies to
     * @param block The highest level class to apply to
     */
    void registerComponentProvider(IComponentProvider dataProvider, TooltipPosition position, Class block);

    /**
     * Registers an {@link IServerDataProvider<TileEntity>} instance for data syncing purposes.
     * A {@link TileEntity} is also an acceptable class type.
     *
     * @param dataProvider The data provider instance
     * @param block The highest level class to apply to
     */
    void registerBlockDataProvider(IServerDataProvider<TileEntity> dataProvider, Class block);

    /**
     * Registers an {@link IEntityComponentProvider} instance to allow overriding the entity being displayed.
     *
     * @param dataProvider The data provider instance
     * @param entity The highest level class to apply to
     */
    void registerOverrideEntityProvider(IEntityComponentProvider dataProvider, Class entity);

    /**
     * Registers an {@link IEntityComponentProvider} instance to allow displaying an item next to the entity name.
     *
     * @param dataProvider The data provider instance
     * @param entity The highest level class to apply to
     */
    void registerEntityStackProvider(IEntityComponentProvider dataProvider, Class entity);

    /**
     * Registers an {@link IEntityComponentProvider} instance for appending {@link net.minecraft.util.text.ITextComponent}
     * to the tooltip.
     *
     * @param dataProvider The data provider instance
     * @param position The position on the tooltip this applies to
     * @param entity The highest level class to apply to
     */
    void registerComponentProvider(IEntityComponentProvider dataProvider, TooltipPosition position, Class entity);

    /**
     * Registers an {@link IEntityComponentProvider} instance for data syncing purposes.
     *
     * @param dataProvider The data provider instance
     * @param entity The highest level class to apply to
     */
    void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class entity);

    /**
     * Registers an {@link IBlockDecorator} instance to allow rendering content in the world while looking at the block.
     *
     * @param decorator The decorator instance
     * @param block The highest level class to apply to
     */
    void registerDecorator(IBlockDecorator decorator, Class block);

    /**
     * Registers an {@link ITooltipRenderer} to allow passing a data string as a component to be rendered as a graphic
     * instead.
     *
     * @param id The ResourceLocation for lookup
     * @param renderer The renderer instance
     */
    void registerTooltipRenderer(ResourceLocation id, ITooltipRenderer renderer);
}
