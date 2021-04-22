package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.overlay.tooltiprenderers.TextTooltipRenderer;
import mcp.mobius.waila.overlay.tooltiprenderers.TooltipRendererArmor;
import mcp.mobius.waila.overlay.tooltiprenderers.TooltipRendererHealth;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.jade.Jade;

public class PluginCore implements IWailaPlugin {

	public static final ResourceLocation RENDER_ENTITY_HEALTH = new ResourceLocation(Waila.MODID, "render_health");
	public static final ResourceLocation RENDER_ENTITY_ARMOR = new ResourceLocation(Waila.MODID, "render_armor");
	public static final ResourceLocation RENDER_TEXT = new ResourceLocation(Waila.MODID, "text");

	public static final ResourceLocation CONFIG_SHOW_REGISTRY = new ResourceLocation(Waila.MODID, "show_registry");
	public static final ResourceLocation CONFIG_SHOW_ENTITY = new ResourceLocation(Waila.MODID, "show_entities");
	public static final ResourceLocation CONFIG_SHOW_ENTITY_HEALTH = new ResourceLocation(Waila.MODID, "show_entity_hp");
	public static final ResourceLocation CONFIG_SHOW_ENTITY_ARMOR = new ResourceLocation(Jade.MODID, "show_entity_armor");
	public static final ResourceLocation CONFIG_SHOW_STATES = new ResourceLocation(Waila.MODID, "show_states");

	@Override
	public void register(IRegistrar registrar) {
		registrar.registerComponentProvider(HUDHandlerBlocks.INSTANCE, TooltipPosition.HEAD, Block.class);
		registrar.registerComponentProvider(HUDHandlerBlocks.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerComponentProvider(HUDHandlerBlocks.INSTANCE, TooltipPosition.TAIL, Block.class);
		registrar.registerBlockDataProvider(HUDHandlerBlocks.INSTANCE, Block.class);

		registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.HEAD, Entity.class);
		registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
		registrar.registerComponentProvider(HUDHandlerEntities.INSTANCE, TooltipPosition.TAIL, Entity.class);

		registrar.addConfig(CONFIG_SHOW_REGISTRY, false);
		registrar.addConfig(CONFIG_SHOW_ENTITY, true);
		registrar.addConfig(CONFIG_SHOW_ENTITY_HEALTH, true);
		registrar.addConfig(CONFIG_SHOW_ENTITY_ARMOR, true);
		registrar.addConfig(CONFIG_SHOW_STATES, false);

		if (FMLEnvironment.dist.isClient()) {
			registrar.registerTooltipRenderer(RENDER_ENTITY_HEALTH, new TooltipRendererHealth());
			registrar.registerTooltipRenderer(RENDER_ENTITY_ARMOR, new TooltipRendererArmor());
			registrar.registerTooltipRenderer(RENDER_TEXT, new TextTooltipRenderer());
		}
	}
}
