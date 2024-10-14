package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Tadpole;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobGrowthProvider implements IEntityComponentProvider, StreamServerDataProvider<EntityAccessor, Integer> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		int time = decodeFromData(accessor).orElse(0);
		if (time > 0) {
			tooltip.add(Component.translatable("jade.mobgrowth.time", IThemeHelper.get().seconds(time, accessor.tickRate())));
		}
	}

	@Override
	public @Nullable Integer streamData(EntityAccessor accessor) {
		int time = -1;
		Entity entity = accessor.getEntity();
		if (entity instanceof AgeableMob ageable) {
			time = -ageable.getAge();
		} else if (entity instanceof Tadpole tadpole) {
			time = tadpole.getTicksLeftUntilAdult();
		}
		return time > 0 ? time : null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_MOB_GROWTH;
	}

}
