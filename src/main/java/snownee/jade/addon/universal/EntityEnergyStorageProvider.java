package snownee.jade.addon.universal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EntityEnergyStorageProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		EnergyStorageProvider.append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		EnergyStorageProvider.putData(accessor);
	}

	@Override
	public ResourceLocation getUid() {
		return EnergyStorageProvider.INSTANCE.getUid();
	}

	@Override
	public int getDefaultPriority() {
		return EnergyStorageProvider.INSTANCE.getDefaultPriority();
	}

}
