package snownee.jade.addon.universal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EntityItemStorageProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemStorageProvider.append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
		ItemStorageProvider.putData(tag, player, entity, showDetails);
	}

	@Override
	public ResourceLocation getUid() {
		return ItemStorageProvider.INSTANCE.getUid();
	}

	@Override
	public int getDefaultPriority() {
		return ItemStorageProvider.INSTANCE.getDefaultPriority();
	}

}
