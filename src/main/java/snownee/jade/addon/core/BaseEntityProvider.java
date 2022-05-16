package snownee.jade.addon.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.WailaConfig;
import snownee.jade.impl.ui.ArmorElement;
import snownee.jade.impl.ui.HealthElement;
import snownee.jade.util.ModIdentification;

public class BaseEntityProvider implements IEntityComponentProvider {

	static final IEntityComponentProvider INSTANCE = new BaseEntityProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		TooltipPosition position = accessor.getTooltipPosition();
		if (position == TooltipPosition.HEAD) {
			appendHead(tooltip, accessor, config);
		} else if (position == TooltipPosition.BODY) {
			appendBody(tooltip, accessor, config);
		} else if (position == TooltipPosition.TAIL) {
			appendTail(tooltip, accessor, config);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHead(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		String name = getEntityName(accessor.getEntity());
		WailaConfig wailaConfig = config.getWailaConfig();
		tooltip.add(new TextComponent(String.format(wailaConfig.getFormatting().getEntityName(), name)).withStyle(wailaConfig.getOverlay().getColor().getTitle()), CorePlugin.TAG_OBJECT_NAME);
		if (config.get(CorePlugin.CONFIG_REGISTRY_NAME))
			tooltip.add(new TextComponent(accessor.getEntity().getType().getRegistryName().toString()).withStyle(ChatFormatting.GRAY), CorePlugin.TAG_REGISTRY_NAME);
	}

	public static String getEntityName(Entity entity) {
		if (!entity.hasCustomName()) {
			if (entity instanceof Villager) {
				return entity.getType().getDescription().getString();
			}
			if (entity instanceof ItemEntity) {
				return ((ItemEntity) entity).getItem().getHoverName().getString();
			}
		}
		return entity.getName().getString();
	}

	@OnlyIn(Dist.CLIENT)
	public void appendBody(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!(accessor.getEntity() instanceof LivingEntity))
			return;
		WailaConfig wailaConfig = config.getWailaConfig();
		if (config.get(CorePlugin.CONFIG_ENTITY_ARMOR))
			appendArmor((LivingEntity) accessor.getEntity(), tooltip, wailaConfig);
		if (config.get(CorePlugin.CONFIG_ENTITY_HEALTH))
			appendHealth((LivingEntity) accessor.getEntity(), tooltip, wailaConfig);
	}

	@OnlyIn(Dist.CLIENT)
	public void appendTail(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(CorePlugin.CONFIG_MOD_NAME))
			return;
		tooltip.add(new TextComponent(String.format(config.getWailaConfig().getFormatting().getModName(), ModIdentification.getModName(accessor.getEntity()))), CorePlugin.TAG_MOD_NAME);
	}

	@OnlyIn(Dist.CLIENT)
	private void appendHealth(LivingEntity living, ITooltip tooltip, WailaConfig config) {
		float health = living.getHealth();
		float maxHealth = living.getMaxHealth();
		tooltip.add(0, new HealthElement(maxHealth, health));
	}

	@OnlyIn(Dist.CLIENT)
	private void appendArmor(LivingEntity living, ITooltip tooltip, WailaConfig config) {
		float armor = living.getArmorValue();
		if (armor == 0)
			return;
		tooltip.add(0, new ArmorElement(armor));
	}

}
