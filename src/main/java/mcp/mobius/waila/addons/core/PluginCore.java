package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.overlay.tooltiprenderers.TooltipRendererHealth;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.util.ResourceLocation;

public class PluginCore implements IWailaPlugin {

    static final ResourceLocation RENDER_ENTITY_HEALTH = new ResourceLocation(Waila.MODID, "render_health");

    static final ResourceLocation CONFIG_SHOW_REGISTRY = new ResourceLocation(Waila.MODID, "show_registry");
    public static final ResourceLocation CONFIG_SHOW_ENTITY = new ResourceLocation(Waila.MODID, "show_entities");
    static final ResourceLocation CONFIG_SHOW_ENTITY_HEALTH = new ResourceLocation(Waila.MODID, "show_entity_hp");
    static final ResourceLocation CONFIG_SHOW_STATES = new ResourceLocation(Waila.MODID, "show_states");

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(HUDHandlerBlocks.INSTANCE, TooltipPosition.HEAD, Block.class);
        registrar.registerComponentProvider(HUDHandlerBlocks.INSTANCE, TooltipPosition.BODY, Block.class);
        registrar.registerComponentProvider(HUDHandlerBlocks.INSTANCE, TooltipPosition.TAIL, Block.class);

        registrar.registerStackProvider(HUDHandlerFluids.INSTANCE, FlowingFluidBlock.class);
        registrar.registerComponentProvider(HUDHandlerFluids.INSTANCE, TooltipPosition.HEAD, FlowingFluidBlock.class);

        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.HEAD, LivingEntity.class);
        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.HEAD, HangingEntity.class);
        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.HEAD, AbstractMinecartEntity.class);
        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.TAIL, LivingEntity.class);
        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.TAIL, HangingEntity.class);
        registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.TAIL, AbstractMinecartEntity.class);

        registrar.addConfig(CONFIG_SHOW_REGISTRY, false);
        registrar.addConfig(CONFIG_SHOW_ENTITY, true);
        registrar.addConfig(CONFIG_SHOW_ENTITY_HEALTH, true);
        registrar.addConfig(CONFIG_SHOW_STATES, false);

        registrar.registerTooltipRenderer(RENDER_ENTITY_HEALTH, new TooltipRendererHealth());
    }
}
