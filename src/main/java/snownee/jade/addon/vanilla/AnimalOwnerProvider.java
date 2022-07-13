package snownee.jade.addon.vanilla;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.PlatformProxy;

public enum AnimalOwnerProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		String name = null;
		if (accessor.getServerData().contains("OwnerName")) {
			name = accessor.getServerData().getString("OwnerName");
		} else {
			Entity entity = accessor.getEntity();
			UUID ownerUUID = null;
			if (entity instanceof OwnableEntity) {
				ownerUUID = ((OwnableEntity) entity).getOwnerUUID();
			} else if (entity instanceof AbstractHorse) {
				ownerUUID = ((AbstractHorse) entity).getOwnerUUID();
			}
			if (ownerUUID != null) {
				name = PlatformProxy.getLastKnownUsername(ownerUUID);
			}
			if (name == null) {
				name = "???";
			}
		}
		tooltip.add(Component.translatable("jade.owner", name));
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
		if (world.getServer().isSingleplayerOwner(player.getGameProfile())) {
			return;
		}
		UUID ownerUUID = null;
		if (entity instanceof OwnableEntity) {
			ownerUUID = ((OwnableEntity) entity).getOwnerUUID();
		} else if (entity instanceof AbstractHorse) {
			ownerUUID = ((AbstractHorse) entity).getOwnerUUID();
		}
		if (ownerUUID != null) {
			String name = PlatformProxy.getLastKnownUsername(ownerUUID);
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
