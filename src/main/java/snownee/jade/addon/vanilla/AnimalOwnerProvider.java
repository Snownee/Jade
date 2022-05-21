package snownee.jade.addon.vanilla;

import java.util.UUID;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.common.UsernameCache;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum AnimalOwnerProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		UUID ownerUUID = null;
		if (entity instanceof OwnableEntity) {
			ownerUUID = ((OwnableEntity) entity).getOwnerUUID();
		} else if (entity instanceof AbstractHorse) {
			ownerUUID = ((AbstractHorse) entity).getOwnerUUID();
		}
		if (ownerUUID != null) {
			String name = UsernameCache.getLastKnownUsername(ownerUUID);
			if (name == null) {
				name = "???";
			}
			tooltip.add(new TranslatableComponent("jade.owner", name));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ANIMAL_OWNER;
	}

}
