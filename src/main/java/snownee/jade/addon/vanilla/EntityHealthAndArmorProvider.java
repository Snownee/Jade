package snownee.jade.addon.vanilla;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.creaking.Creaking;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.ArmorElement;
import snownee.jade.impl.ui.HealthElement;

public class EntityHealthAndArmorProvider implements StreamServerDataProvider<EntityAccessor, Float> {
	public static final EntityHealthAndArmorProvider INSTANCE = new EntityHealthAndArmorProvider();

	@Override
	public @Nullable Float streamData(EntityAccessor accessor) {
		float absorption = ((LivingEntity) accessor.getEntity()).getAbsorptionAmount();
		return absorption > 0 ? absorption : 0;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Float> streamCodec() {
		return ByteBufCodecs.FLOAT.cast();
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		return EntityHealthAndArmorProvider.isHealthVisible((LivingEntity) accessor.getEntity());
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_ENTITY_HEALTH;
	}

	@Override
	public int getDefaultPriority() {
		return -8000;
	}

	private static boolean isHealthVisible(LivingEntity entity) {
		return !(entity instanceof ArmorStand || entity instanceof Creaking);
	}

	public static class Client extends EntityHealthAndArmorProvider implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			boolean healthText = false;
			boolean armorText = false;
			List<Element> elements = Lists.newArrayListWithExpectedSize(2);
			LivingEntity living = (LivingEntity) accessor.getEntity();
			if (config.get(JadeIds.MC_ENTITY_HEALTH) && isHealthVisible(living)) {
				float health = living.getHealth();
				float maxHealth = living.getMaxHealth();
				float absorption = decodeFromData(accessor).orElse(0F);
				HealthElement healthElement = new HealthElement(
						living.isFullyFrozen() ? Gui.HeartType.FROZEN : Gui.HeartType.NORMAL,
						maxHealth,
						health,
						absorption);
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
				tooltip.append(JadeUI.spacer(4, 0));
				tooltip.append(elements.get(1));
			} else {
				elements.forEach(tooltip::add);
			}
		}

		@Override
		public boolean isRequired() {
			return true;
		}
	}
}
