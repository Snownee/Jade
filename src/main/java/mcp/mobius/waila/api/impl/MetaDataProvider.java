package mcp.mobius.waila.api.impl;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.network.MessageRequestEntity;
import mcp.mobius.waila.network.MessageRequestTile;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MetaDataProvider {

    public static int rateLimiter = 250;

    private Map<Integer, List<IComponentProvider>> headBlockProviders = new TreeMap<>();
    private Map<Integer, List<IComponentProvider>> bodyBlockProviders = new TreeMap<>();
    private Map<Integer, List<IComponentProvider>> tailBlockProviders = new TreeMap<>();

    private Map<Integer, List<IEntityComponentProvider>> headEntityProviders = new TreeMap<>();
    private Map<Integer, List<IEntityComponentProvider>> bodyEntityProviders = new TreeMap<>();
    private Map<Integer, List<IEntityComponentProvider>> tailEntityProviders = new TreeMap<>();

    public void gatherBlockComponents(DataAccessor accessor, List<ITextComponent> tooltip, TooltipPosition position) {
        Block block = accessor.getBlock();

        if (accessor.getTileEntity() != null && accessor.isTimeElapsed(rateLimiter) && Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
            accessor.resetTimer();
            if (WailaRegistrar.INSTANCE.hasNBTProviders(block) || WailaRegistrar.INSTANCE.hasNBTProviders(accessor.getTileEntity()))
                Waila.NETWORK.sendToServer(new MessageRequestTile(accessor.getTileEntity()));
        }

        headBlockProviders.clear();
        bodyBlockProviders.clear();
        tailBlockProviders.clear();

		/* Lookup by class (for blocks)*/
        if (position == TooltipPosition.HEAD && WailaRegistrar.INSTANCE.hasHeadProviders(block))
            headBlockProviders.putAll(WailaRegistrar.INSTANCE.getHeadProviders(block));

        else if (position == TooltipPosition.BODY && WailaRegistrar.INSTANCE.hasBodyProviders(block))
            bodyBlockProviders.putAll(WailaRegistrar.INSTANCE.getBodyProviders(block));

        else if (position == TooltipPosition.TAIL && WailaRegistrar.INSTANCE.hasTailProviders(block))
            tailBlockProviders.putAll(WailaRegistrar.INSTANCE.getTailProviders(block));

		
		/* Lookup by class (for tileentities)*/
        if (position == TooltipPosition.HEAD && WailaRegistrar.INSTANCE.hasHeadProviders(accessor.getTileEntity()))
            headBlockProviders.putAll(WailaRegistrar.INSTANCE.getHeadProviders(accessor.getTileEntity()));

        else if (position == TooltipPosition.BODY && WailaRegistrar.INSTANCE.hasBodyProviders(accessor.getTileEntity()))
            bodyBlockProviders.putAll(WailaRegistrar.INSTANCE.getBodyProviders(accessor.getTileEntity()));

        else if (position == TooltipPosition.TAIL && WailaRegistrar.INSTANCE.hasTailProviders(accessor.getTileEntity()))
            tailBlockProviders.putAll(WailaRegistrar.INSTANCE.getTailProviders(accessor.getTileEntity()));

		/* Apply all collected providers */

        switch (position) {
            case HEAD: {
                for (List<IComponentProvider> providersList : headBlockProviders.values()) {
                    for (IComponentProvider dataProvider : providersList) {
                        try {
                            dataProvider.appendHead(tooltip, accessor, PluginConfig.INSTANCE);
                        } catch (Throwable e) {
                            WailaExceptionHandler.handleErr(e, dataProvider.getClass().toString(), tooltip);
                        }
                    }
                }
                break;
            }
            case BODY: {
                for (List<IComponentProvider> providersList : bodyBlockProviders.values()) {
                    for (IComponentProvider dataProvider : providersList) {
                        try {
                            dataProvider.appendBody(tooltip, accessor, PluginConfig.INSTANCE);
                        } catch (Throwable e) {
                            WailaExceptionHandler.handleErr(e, dataProvider.getClass().toString(), tooltip);
                        }
                    }
                }
                break;
            }
            case TAIL: {
                for (List<IComponentProvider> providersList : tailBlockProviders.values()) {
                    for (IComponentProvider dataProvider : providersList) {
                        try {
                            dataProvider.appendTail(tooltip, accessor, PluginConfig.INSTANCE);
                        } catch (Throwable e) {
                            WailaExceptionHandler.handleErr(e, dataProvider.getClass().toString(), tooltip);
                        }
                    }
                }
                break;
            }
        }
    }

    public void gatherEntityComponents(Entity entity, DataAccessor accessor, List<ITextComponent> tooltip, TooltipPosition position) {
        if (accessor.getEntity() != null && accessor.isTimeElapsed(rateLimiter)) {
            accessor.resetTimer();

            if (WailaRegistrar.INSTANCE.hasNBTEntityProviders(accessor.getEntity()))
                Waila.NETWORK.sendToServer(new MessageRequestEntity(accessor.getEntity()));
        }

        headEntityProviders.clear();
        bodyEntityProviders.clear();
        tailEntityProviders.clear();

		/* Lookup by class (for entities)*/
        if (position == TooltipPosition.HEAD && WailaRegistrar.INSTANCE.hasHeadEntityProviders(entity))
            headEntityProviders.putAll(WailaRegistrar.INSTANCE.getHeadEntityProviders(entity));

        else if (position == TooltipPosition.BODY && WailaRegistrar.INSTANCE.hasBodyEntityProviders(entity))
            bodyEntityProviders.putAll(WailaRegistrar.INSTANCE.getBodyEntityProviders(entity));

        else if (position == TooltipPosition.TAIL && WailaRegistrar.INSTANCE.hasTailEntityProviders(entity))
            tailEntityProviders.putAll(WailaRegistrar.INSTANCE.getTailEntityProviders(entity));

        switch (position) {
            case HEAD: {
                for (List<IEntityComponentProvider> providersList : headEntityProviders.values()) {
                    for (IEntityComponentProvider dataProvider : providersList) {
                        try {
                            dataProvider.appendHead(tooltip, accessor, PluginConfig.INSTANCE);
                        } catch (Throwable e) {
                            WailaExceptionHandler.handleErr(e, dataProvider.getClass().toString(), tooltip);
                        }
                    }
                }
                break;
            }
            case BODY: {
                for (List<IEntityComponentProvider> providersList : bodyEntityProviders.values()) {
                    for (IEntityComponentProvider dataProvider : providersList) {
                        try {
                            dataProvider.appendBody(tooltip, accessor, PluginConfig.INSTANCE);
                        } catch (Throwable e) {
                            WailaExceptionHandler.handleErr(e, dataProvider.getClass().toString(), tooltip);
                        }
                    }
                }
                break;
            }
            case TAIL: {
                for (List<IEntityComponentProvider> providersList : tailEntityProviders.values()) {
                    for (IEntityComponentProvider dataProvider : providersList) {
                        try {
                            dataProvider.appendTail(tooltip, accessor, PluginConfig.INSTANCE);
                        } catch (Throwable e) {
                            WailaExceptionHandler.handleErr(e, dataProvider.getClass().toString(), tooltip);
                        }
                    }
                }
                break;
            }
        }
    }
}
