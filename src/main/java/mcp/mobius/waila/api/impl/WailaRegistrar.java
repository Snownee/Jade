package mcp.mobius.waila.api.impl;

import java.util.EnumMap;
import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.impl.config.ConfigEntry;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class WailaRegistrar implements IRegistrar {

    public static final WailaRegistrar INSTANCE = new WailaRegistrar();

    final HierarchyLookup<IComponentProvider> blockStackProviders;
    final EnumMap<TooltipPosition, HierarchyLookup<IComponentProvider>> blockComponentProviders;
    final HierarchyLookup<IServerDataProvider<TileEntity>> blockDataProviders;

    final HierarchyLookup<IEntityComponentProvider> entityOverrideProviders;
    final HierarchyLookup<IEntityComponentProvider> entityStackProviders;
    final EnumMap<TooltipPosition, HierarchyLookup<IEntityComponentProvider>> entityComponentProviders;
    final HierarchyLookup<IServerDataProvider<Entity>> entityDataProviders;

    WailaRegistrar() {
        blockStackProviders = new HierarchyLookup<>(Block.class);
        blockComponentProviders = new EnumMap<>(TooltipPosition.class);
        blockDataProviders = new HierarchyLookup<>(TileEntity.class);

        entityOverrideProviders = new HierarchyLookup<>(Entity.class);
        entityStackProviders = new HierarchyLookup<>(Entity.class);
        entityComponentProviders = new EnumMap<>(TooltipPosition.class);
        entityDataProviders = new HierarchyLookup<>(Entity.class);

        for (TooltipPosition position : TooltipPosition.values()) {
            blockComponentProviders.put(position, new HierarchyLookup<>(Block.class));
            entityComponentProviders.put(position, new HierarchyLookup<>(Entity.class));
        }
    }

    /* CONFIG HANDLING */

    @Override
    public void addConfig(ResourceLocation key, boolean defaultValue) {
        PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, false));
    }

    @Override
    public void addSyncedConfig(ResourceLocation key, boolean defaultValue) {
        PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, true));
    }

    /* REGISTRATION METHODS */

    @Override
    public void registerStackProvider(IComponentProvider dataProvider, Class<? extends Block> block) {
        blockStackProviders.register(block, dataProvider);
    }

    @Override
    public void registerComponentProvider(IComponentProvider dataProvider, TooltipPosition position, Class<? extends Block> block) {
        blockComponentProviders.get(position).register(block, dataProvider);
    }

    @Override
    public void registerBlockDataProvider(IServerDataProvider<TileEntity> dataProvider, Class<? extends TileEntity> block) {
        blockDataProviders.register(block, dataProvider);
    }

    @Override
    public void registerOverrideEntityProvider(IEntityComponentProvider dataProvider, Class<? extends Entity> entity) {
        entityOverrideProviders.register(entity, dataProvider);
    }

    @Override
    public void registerEntityStackProvider(IEntityComponentProvider dataProvider, Class<? extends Entity> entity) {
        entityStackProviders.register(entity, dataProvider);
    }

    @Override
    public void registerComponentProvider(IEntityComponentProvider dataProvider, TooltipPosition position, Class<? extends Entity> entity) {
        entityComponentProviders.get(position).register(entity, dataProvider);
    }

    @Override
    public void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity) {
        entityDataProviders.register(entity, dataProvider);
    }

    /* PROVIDER GETTERS */

    public List<IComponentProvider> getBlockProviders(Block block, TooltipPosition position) {
        return blockComponentProviders.get(position).get(block);
    }

    public List<IComponentProvider> getBlockStackProviders(Block block) {
        return blockStackProviders.get(block);
    }

    public List<IServerDataProvider<TileEntity>> getBlockNBTProviders(TileEntity block) {
        return blockDataProviders.get(block);
    }

    public List<IEntityComponentProvider> getEntityProviders(Entity entity, TooltipPosition position) {
        return entityComponentProviders.get(position).get(entity);
    }

    public List<IEntityComponentProvider> getOverrideEntityProviders(Entity entity) {
        return entityOverrideProviders.get(entity);
    }

    public List<IEntityComponentProvider> getEntityStackProviders(Entity entity) {
        return entityStackProviders.get(entity);
    }

    public List<IServerDataProvider<Entity>> getEntityNBTProviders(Entity entity) {
        return entityDataProviders.get(entity);
    }

}
