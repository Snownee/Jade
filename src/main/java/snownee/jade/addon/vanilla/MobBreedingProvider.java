package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobBreedingProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("BreedingCD", Tag.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("BreedingCD");
		if (time > 0) {
			tooltip.add(Component.translatable(
					accessor.getEntity() instanceof Allay ? "jade.mobduplication.time" : "jade.mobbreeding.time",
					IThemeHelper.get().seconds(time, accessor.tickRate())));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		int time = 0;
		Entity entity = accessor.getEntity();
		if (entity instanceof Allay allay) {
			if (allay.duplicationCooldown > 0 && allay.duplicationCooldown < Integer.MAX_VALUE) {
				time = (int) allay.duplicationCooldown;
			}
		} else {
			time = ((Animal) entity).getAge();
		}
		if (time > 0) {
			tag.putInt("BreedingCD", time);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_MOB_BREEDING;
	}
}
