package snownee.jade.addon.vanilla;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.CommonProxy;

public enum AnimalOwnerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		String name;
		if (accessor.getServerData().contains("OwnerName")) {
			name = accessor.getServerData().getString("OwnerName");
		} else {
			UUID ownerUUID = getOwnerUUID(accessor.getEntity());
			if (ownerUUID == null) {
				return;
			}
			name = CommonProxy.getLastKnownUsername(ownerUUID);
			if (name == null) {
				name = "???";
			}
		}
		tooltip.add(Component.translatable("jade.owner", name));
	}

	@Override
	public void appendServerData(CompoundTag data, EntityAccessor accessor) {
		UUID ownerUUID = getOwnerUUID(accessor.getEntity());
		if (ownerUUID != null) {
			String name = CommonProxy.getLastKnownUsername(ownerUUID);
			if (name != null) {
				data.putString("OwnerName", name);
			}
		}
	}

	public static UUID getOwnerUUID(Entity entity) {
		if (entity instanceof OwnableEntity ownableEntity) {
			return ownableEntity.getOwnerUUID();
		}
		return null;
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		Entity entity = accessor.getEntity();
		return entity instanceof OwnableEntity && getOwnerUUID(entity) == null;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_ANIMAL_OWNER;
	}

}
