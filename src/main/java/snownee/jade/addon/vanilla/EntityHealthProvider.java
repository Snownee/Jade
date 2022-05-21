package snownee.jade.addon.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.HealthElement;

public enum EntityHealthProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		LivingEntity living = (LivingEntity) accessor.getEntity();
		float health = living.getHealth();
		float maxHealth = living.getMaxHealth();
		tooltip.add(new HealthElement(maxHealth, health));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ENTITY_HEALTH;
	}

	@Override
	public int getDefaultPriority() {
		return -4501;
	}

}
