package snownee.jade.addon.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.ArmorElement;

public enum EntityArmorProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		LivingEntity living = (LivingEntity) accessor.getEntity();
		float armor = living.getArmorValue();
		if (armor == 0)
			return;
		tooltip.add(new ArmorElement(armor));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ENTITY_ARMOR;
	}

	@Override
	public int getDefaultPriority() {
		return -4500;
	}

}
