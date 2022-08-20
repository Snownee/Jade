package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import snownee.jade.JadeCommonConfig;
import snownee.jade.addon.fabric.BlockInventoryProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.PlatformProxy;

public enum ContainerEntityProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		BlockInventoryProvider.append(tooltip, accessor);
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, Entity t, boolean showDetails) {
		int size = 54;
		if (t instanceof AbstractChestedHorse horse && horse.hasChest()) {
			PlatformProxy.putHorseInvData(horse, data, size);
			return;
		} else if (t instanceof ContainerEntity entity) {
			if (entity.getLootTable() != null) {
				data.putBoolean("Loot", true);
				return;
			}
			if (!entity.isEmpty()) {
				BlockInventoryProvider.putInvData(data, entity, size, 0);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_CONTAINER_ENTITY;
	}
}
