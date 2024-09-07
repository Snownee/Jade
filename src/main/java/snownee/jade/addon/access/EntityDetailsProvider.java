package snownee.jade.addon.access;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class EntityDetailsProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		if (entity instanceof Creeper creeper && creeper.isPowered()) {
			AccessibilityPlugin.replaceTitle(tooltip, "creeper.powered");
		} else if (entity instanceof WitherBoss witherBoss && witherBoss.isPowered()) {
			AccessibilityPlugin.replaceTitle(tooltip, "wither.powered");
		} else if (entity instanceof Sheep sheep) {
			AccessibilityPlugin.replaceTitle(tooltip, "entity." + sheep.getColor().getSerializedName());
		}
		if (entity instanceof LivingEntity livingEntity && livingEntity.isBaby()) {
			AccessibilityPlugin.replaceTitle(tooltip, "entity.baby");
		}
		if (entity instanceof Shearable shearable && !shearable.readyForShearing()) {
			AccessibilityPlugin.replaceTitle(tooltip, "entity.sheared");
		}
		if (entity instanceof Saddleable saddleable && saddleable.isSaddled()) {
			AccessibilityPlugin.replaceTitle(tooltip, "entity.saddled");
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_ENTITY_DETAILS;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.getEntity().getDefaultPriority() + 10;
	}
}
