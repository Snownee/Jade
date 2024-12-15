package snownee.jade.addon.access;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.ZombieVillager;
import snownee.jade.JadeClient;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class EntityDetailsProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		String objectName = tooltip.getMessage(JadeIds.CORE_OBJECT_NAME);
		if (entity instanceof Creeper creeper && creeper.isPowered()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "creeper.powered");
		} else if (entity instanceof WitherBoss witherBoss && witherBoss.isPowered()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "wither.powered");
		} else if (entity instanceof ZombieVillager zombieVillager && zombieVillager.isConverting()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "zombie_villager.curing");
		} else if (entity instanceof Goat goat && !goat.hasLeftHorn() && !goat.hasRightHorn()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "goat.hornless");
		} else if (entity instanceof Sheep sheep) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity." + sheep.getColor().getSerializedName());
		} else if (entity instanceof Bee bee) {
			if (bee.hasNectar()) {
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "bee.nectar");
			}
			if (bee.isAngry()) {
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.angry");
			}
		} else if (entity instanceof Slime slime) {
			String message = tooltip.getMessage(JadeIds.CORE_OBJECT_NAME);
			Component title = IThemeHelper.get().title(JadeClient.format("jade.access.slime.size", message, slime.getSize()));
			tooltip.replace(JadeIds.CORE_OBJECT_NAME, title);
		}
		if (entity instanceof LivingEntity livingEntity && livingEntity.isBaby()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.baby");
		}
		if (entity instanceof Shearable shearable && !shearable.readyForShearing()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.sheared");
		}
		if (entity instanceof Saddleable saddleable && saddleable.isSaddled()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.saddled");
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
