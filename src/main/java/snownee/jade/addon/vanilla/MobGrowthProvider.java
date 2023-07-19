package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Tadpole;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum MobGrowthProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("GrowingTime", Tag.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("GrowingTime");
		if (time > 0) {
			tooltip.add(Component.translatable("jade.mobgrowth.time", time / 20));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		int time = -1;
		Entity entity = accessor.getEntity();
		if (entity instanceof AgeableMob ageable) {
			time = -ageable.getAge();
		} else if (entity instanceof Tadpole tadpole) {
			time = tadpole.getTicksLeftUntilAdult();
		}
		if (time > 0) {
			tag.putInt("GrowingTime", time);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_MOB_GROWTH;
	}

}
