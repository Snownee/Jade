package snownee.jade.addon.vanilla;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ArmorElement;
import snownee.jade.impl.ui.HealthElement;

public enum EntityHealthAndArmorProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		boolean healthText = false;
		boolean armorText = false;
		List<IElement> elements = Lists.newArrayListWithExpectedSize(2);
		LivingEntity living = (LivingEntity) accessor.getEntity();
		if (config.get(JadeIds.MC_ENTITY_HEALTH) && !(accessor.getEntity() instanceof ArmorStand)) {
			float health = living.getHealth();
			float maxHealth = living.getMaxHealth();
			HealthElement healthElement = new HealthElement(maxHealth, health);
			elements.add(healthElement.tag(JadeIds.MC_ENTITY_HEALTH));
			healthText = healthElement.showText();
		}
		if (config.get(JadeIds.MC_ENTITY_ARMOR) && living.getArmorValue() > 0) {
			ArmorElement armorElement = new ArmorElement(living.getArmorValue());
			elements.add(armorElement.tag(JadeIds.MC_ENTITY_ARMOR));
			armorText = armorElement.showText();
		}
		if (healthText && armorText) {
			tooltip.add(elements.get(0));
			tooltip.append(IElementHelper.get().spacer(4, 0));
			tooltip.append(elements.get(1));
		} else {
			elements.forEach(tooltip::add);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_ENTITY_HEALTH;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return -4500;
	}

}
