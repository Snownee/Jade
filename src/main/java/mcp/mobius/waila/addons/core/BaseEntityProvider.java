package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.impl.ui.ArmorElement;
import mcp.mobius.waila.impl.ui.HealthElement;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

public class BaseEntityProvider implements IEntityComponentProvider {

	static final IEntityComponentProvider INSTANCE = new BaseEntityProvider();

	@Override
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

	public void appendHead(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		String name = getEntityName(accessor.getEntity());
		WailaConfig wailaConfig = Waila.CONFIG.get();
		tooltip.add(new TextComponent(String.format(wailaConfig.getFormatting().getEntityName(), name)).withStyle(wailaConfig.getOverlay().getColor().getTitle()), CorePlugin.TAG_OBJECT_NAME);
		if (config.get(CorePlugin.CONFIG_REGISTRY_NAME))
			tooltip.add(new TextComponent(accessor.getEntity().getType().getRegistryName().toString()).withStyle(ChatFormatting.GRAY), CorePlugin.TAG_REGISTRY_NAME);
	}

	public static String getEntityName(Entity entity) {
		if (entity instanceof Villager && !entity.hasCustomName()) {
			return entity.getType().getDescription().getString();
		}
		return entity.getName().getString();
	}

	public void appendBody(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!(accessor.getEntity() instanceof LivingEntity))
			return;
		if (config.get(CorePlugin.CONFIG_ENTITY_HEALTH))
			appendHealth((LivingEntity) accessor.getEntity(), tooltip);
		if (config.get(CorePlugin.CONFIG_ENTITY_HEALTH))
			appendArmor((LivingEntity) accessor.getEntity(), tooltip);
	}

	public void appendTail(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(CorePlugin.CONFIG_MOD_NAME))
			return;
		tooltip.add(new TextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(accessor.getEntity()))));
	}

	private void appendHealth(LivingEntity living, ITooltip tooltip) {
		float health = living.getHealth();
		float maxHealth = living.getMaxHealth();

		if (living.getMaxHealth() > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
			HealthElement icon = new HealthElement(1, 1);
			Component text = new TextComponent(String.format("  %s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth)));
			tooltip.add(icon);
			tooltip.append(text);
		} else {
			tooltip.add(new HealthElement(maxHealth * 0.5F, health * 0.5F));
		}
	}

	private void appendArmor(LivingEntity living, ITooltip tooltip) {
		float armor = living.getArmorValue();
		if (armor == 0)
			return;
		if (armor > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
			ArmorElement icon = new ArmorElement(-1);
			Component text = new TextComponent(DisplayHelper.dfCommas.format(armor));
			tooltip.add(icon);
			tooltip.append(text);
		} else {
			tooltip.add(new ArmorElement(armor * 0.5F));
		}
	}

}
