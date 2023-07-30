package snownee.jade.addon.vanilla;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
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
			Entity entity = accessor.getEntity();
			UUID ownerUUID = null;
			if (entity instanceof OwnableEntity) {
				ownerUUID = ((OwnableEntity) entity).getOwnerUUID();
			}
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
		MinecraftServer server = accessor.getLevel().getServer();
		if (server != null && server.isSingleplayerOwner(accessor.getPlayer().getGameProfile()) && accessor.getEntity() instanceof TamableAnimal) {
			return;
		}
		Entity entity = accessor.getEntity();
		UUID ownerUUID = null;
		if (entity instanceof OwnableEntity) {
			ownerUUID = ((OwnableEntity) entity).getOwnerUUID();
		}
		if (ownerUUID != null) {
			String name = CommonProxy.getLastKnownUsername(ownerUUID);
			if (name != null) {
				data.putString("OwnerName", name);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ANIMAL_OWNER;
	}

}
