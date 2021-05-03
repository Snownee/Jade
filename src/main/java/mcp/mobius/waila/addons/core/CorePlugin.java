package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import snownee.jade.Jade;

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
	public void register(IRegistrar registrar) {
		registrar.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.HEAD, Block.class);
		registrar.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerComponentProvider(BaseBlockProvider.INSTANCE, TooltipPosition.TAIL, Block.class);
		registrar.registerBlockDataProvider(BaseBlockProvider.INSTANCE, TileEntity.class);

		registrar.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.HEAD, Entity.class);
		registrar.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
		registrar.registerComponentProvider(BaseEntityProvider.INSTANCE, TooltipPosition.TAIL, Entity.class);

		registrar.addConfig(CONFIG_REGISTRY_NAME, false);
		registrar.addConfig(CONFIG_ENTITY_HEALTH, true);
		registrar.addConfig(CONFIG_ENTITY_ARMOR, true);
		registrar.addConfig(CONFIG_BLOCK_STATES, false);
		registrar.addConfig(CONFIG_MOD_NAME, true);
		registrar.addConfig(CONFIG_ITEM_MOD_NAME, false);
	}
}
