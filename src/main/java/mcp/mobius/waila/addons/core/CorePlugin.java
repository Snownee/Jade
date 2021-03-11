package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import snownee.jade.Jade;

public class CorePlugin implements IWailaPlugin {

    public static final ResourceLocation RENDER_ENTITY_HEALTH = new ResourceLocation(Waila.MODID, "render_health");
    public static final ResourceLocation RENDER_ENTITY_ARMOR = new ResourceLocation(Waila.MODID, "render_armor");
    public static final ResourceLocation RENDER_TEXT = new ResourceLocation(Waila.MODID, "text");

    public static final ResourceLocation CONFIG_SHOW_REGISTRY = new ResourceLocation(Waila.MODID, "show_registry");
    public static final ResourceLocation CONFIG_SHOW_ENTITY = new ResourceLocation(Waila.MODID, "show_entities");
    public static final ResourceLocation CONFIG_SHOW_ENTITY_HEALTH = new ResourceLocation(Waila.MODID, "show_entity_hp");
    public static final ResourceLocation CONFIG_SHOW_ENTITY_ARMOR = new ResourceLocation(Jade.MODID, "show_entity_armor");
    public static final ResourceLocation CONFIG_SHOW_STATES = new ResourceLocation(Waila.MODID, "show_states");

    public static final ResourceLocation TAG_OBJECT_NAME = new ResourceLocation(Waila.MODID, "object_name");
    public static final ResourceLocation TAG_REGISTRY_NAME = new ResourceLocation(Waila.MODID, "registry_name");
    public static final ResourceLocation TAG_MOD_NAME = new ResourceLocation(Waila.MODID, "mod_name");

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.HEAD, Block.class);
        registrar.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.BODY, Block.class);
        registrar.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.TAIL, Block.class);
        registrar.registerBlockDataProvider(BaseBlockProvider.INSTANCE, TileEntity.class);

        registrar.registerStackProvider(BaseFluidProvider.INSTANCE, FlowingFluidBlock.class);
        registrar.registerComponentProvider(BaseFluidProvider.INSTANCE, TooltipPosition.HEAD, FlowingFluidBlock.class);

        registrar.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.HEAD, Entity.class);
        registrar.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
        registrar.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.TAIL, Entity.class);
        registrar.registerEntityStackProvider(BaseEntityProvider.INSTANCE, Entity.class);

        registrar.addConfig(CONFIG_SHOW_REGISTRY, false);
        registrar.addConfig(CONFIG_SHOW_ENTITY, true);
        registrar.addConfig(CONFIG_SHOW_ENTITY_HEALTH, true);
        registrar.addConfig(CONFIG_SHOW_ENTITY_ARMOR, true);
        registrar.addConfig(CONFIG_SHOW_STATES, false);
    }
}
