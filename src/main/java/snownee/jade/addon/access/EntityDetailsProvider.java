package snownee.jade.addon.access;

import com.mojang.datafixers.util.Either;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.level.block.WeatheringCopper;
import snownee.jade.JadeClient;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.CommonProxy;

public class EntityDetailsProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		String objectName = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
		switch (entity) {
			case Creeper creeper when creeper.isPowered() -> AccessibilityPlugin.replaceTitle(tooltip, objectName, "creeper.powered");
			case WitherBoss witherBoss when witherBoss.isPowered() -> AccessibilityPlugin.replaceTitle(
					tooltip,
					objectName,
					"wither.powered");
			case ZombieVillager zombieVillager when zombieVillager.isConverting() -> AccessibilityPlugin.replaceTitle(
					tooltip,
					objectName,
					"zombie_villager.curing");
			case Goat goat when !goat.hasLeftHorn() && !goat.hasRightHorn() -> AccessibilityPlugin.replaceTitle(
					tooltip,
					objectName,
					"goat.hornless");
			case Bee bee -> {
				if (bee.hasNectar()) {
					AccessibilityPlugin.replaceTitle(tooltip, objectName, "bee.nectar");
				}
				if (bee.isAngry()) {
					AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.angry");
				}
			}
			case AbstractCubeMob slime -> {
				String message = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
				Component title = IThemeHelper.get().title(JadeClient.format("jade.access.slime.size", message, slime.getSize()));
				tooltip.replace(JadeIds.CORE_OBJECT_NAME, title);
			}
			case CopperGolem golem -> {
				WeatheringCopper.WeatherState state = golem.getWeatherState();
				if (state != WeatheringCopper.WeatherState.UNAFFECTED) {
					AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity." + state.getSerializedName());
				}
			}
			default -> {
			}
		}
		if (entity instanceof LivingEntity livingEntity && livingEntity.isBaby()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.baby");
		}
		TriState shearable = CommonProxy.isShearable(entity);
		if (shearable != TriState.DEFAULT) {
			if (entity instanceof CopperGolem) {
				if (shearable == TriState.TRUE) {
					AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.shearable");
				}
			} else {
				if (shearable == TriState.FALSE) {
					AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.sheared");
				}
			}
		}
		if (entity instanceof Mob mob && mob.isSaddled()) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity.saddled");
		}

		Either<String, Component> color = EntityVariantHelper.getVariantName(entity, true);
		if (color != null) {
			color.ifLeft(s -> AccessibilityPlugin.replaceTitle(tooltip, objectName, "entity." + s));
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.ACCESS_ENTITY_DETAILS;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.ForEntity.INSTANCE.getDefaultPriority() + 10;
	}
}
