package snownee.jade.addon.universal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EntityProgressProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ProgressProvider.append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		ProgressProvider.putData(accessor);
	}

	@Override
	public ResourceLocation getUid() {
		return ProgressProvider.INSTANCE.getUid();
	}

	@Override
	public int getDefaultPriority() {
		return ProgressProvider.INSTANCE.getDefaultPriority();
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
