package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.Jade;

@WailaPlugin(priority = -100)
public class CorePlugin implements IWailaPlugin {

	private static ResourceLocation JADE(String path) {
		return new ResourceLocation(Jade.MODID, path);
	}

	public static final ResourceLocation CONFIG_REGISTRY_NAME = JADE("registry_name");
	public static final ResourceLocation CONFIG_ENTITY_HEALTH = JADE("entity_hp");
	public static final ResourceLocation CONFIG_ENTITY_ARMOR = JADE("entity_armor");
	public static final ResourceLocation CONFIG_BLOCK_STATES = JADE("block_states");
	public static final ResourceLocation CONFIG_MOD_NAME = JADE("mod_name");
	public static final ResourceLocation CONFIG_ITEM_MOD_NAME = JADE("item_mod_name");

	public static final ResourceLocation TAG_OBJECT_NAME = new ResourceLocation(Waila.MODID, "object_name");
	public static final ResourceLocation TAG_REGISTRY_NAME = new ResourceLocation(Waila.MODID, "registry_name");
	public static final ResourceLocation TAG_MOD_NAME = new ResourceLocation(Waila.MODID, "mod_name");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BaseBlockProvider.INSTANCE, BlockEntity.class);

		registration.addConfig(CONFIG_REGISTRY_NAME, false);
		registration.addConfig(CONFIG_ENTITY_HEALTH, true);
		registration.addConfig(CONFIG_ENTITY_ARMOR, true);
		registration.addConfig(CONFIG_BLOCK_STATES, false);
		registration.addConfig(CONFIG_MOD_NAME, true);
		registration.addConfig(CONFIG_ITEM_MOD_NAME, false);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.HEAD, Block.class);
		registration.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.TAIL, Block.class);

		registration.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.HEAD, Entity.class);
		registration.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
		registration.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.TAIL, Entity.class);

		registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);
		registration.hideTarget(EntityType.FIREWORK_ROCKET);
	}
}
