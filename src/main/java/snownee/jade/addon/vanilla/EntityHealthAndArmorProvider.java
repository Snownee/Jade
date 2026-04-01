package snownee.jade.addon.vanilla;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.livingblock.LivingBlock;
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
		return EntityHealthAndArmorProvider.isHealthVisible(accessor.getEntity());
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ENTITY_HEALTH;
	}

	@Override
	public int getDefaultPriority() {
		return -8000;
	}

	private static boolean isHealthVisible(Entity entity) {
		if (entity instanceof LivingEntity) {
			return !(entity instanceof ArmorStand || entity instanceof Creaking);
		}
		return entity instanceof LivingBlock;
	}

	public static class Client extends EntityHealthAndArmorProvider implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			boolean healthText = false;
			boolean armorText = false;
			List<Element> elements = Lists.newArrayListWithExpectedSize(2);
			ClientData data = getClientData(accessor.getEntity());
			if (data == null) {
				return;
			}
			if (config.get(JadeIds.MC_ENTITY_HEALTH) && data.maxHealth > 0) {
				float absorption = decodeFromData(accessor).orElse(0F);
				HealthElement healthElement = new HealthElement(
						accessor.getEntity().isFullyFrozen() ? Gui.HeartType.FROZEN : Gui.HeartType.NORMAL,
						data.maxHealth,
						data.health,
						absorption);
				elements.add(healthElement.tag(JadeIds.MC_ENTITY_HEALTH));
				healthText = healthElement.showText();
			}
			if (config.get(JadeIds.MC_ENTITY_ARMOR) && data.armor > 0) {
				ArmorElement armorElement = new ArmorElement(data.armor);
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

		@Nullable
		private ClientData getClientData(Entity entity) {
			if (entity instanceof LivingEntity living) {
				if (isHealthVisible(living)) {
					return new ClientData(living.getHealth(), living.getMaxHealth(), living.getArmorValue());
				} else {
					return new ClientData(0, 0, living.getArmorValue());
				}
			}
			if (entity instanceof LivingBlock livingBlock) {
				return new ClientData(livingBlock.getHealth(), livingBlock.getMaxHealth(), 0);
			}
			return null;
		}

		@Override
		public boolean isRequired() {
			return true;
		}
	}

	public record ClientData(float health, float maxHealth, float armor) {}
}
