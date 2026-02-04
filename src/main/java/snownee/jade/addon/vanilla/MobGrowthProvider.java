package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
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

public class MobGrowthProvider implements StreamServerDataProvider<EntityAccessor, Integer> {
	public static final MobGrowthProvider INSTANCE = new MobGrowthProvider();

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
	public boolean shouldRequestData(EntityAccessor accessor) {
		Entity entity = accessor.getEntity();
		if (entity instanceof AgeableMob ageable) {
			return ageable.isBaby() && !ageable.isAgeLocked();
		} else if (entity instanceof Tadpole tadpole) {
			return !tadpole.isAgeLocked();
		}
		return false;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_MOB_GROWTH;
	}

	public static class Client implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			boolean ageLocked = false;
			Entity entity = accessor.getEntity();
			if (entity instanceof AgeableMob ageable) {
				ageLocked = ageable.isBaby() && ageable.isAgeLocked();
			} else if (entity instanceof Tadpole tadpole) {
				ageLocked = tadpole.isAgeLocked();
			}
			if (ageLocked) {
				tooltip.add(Component.translatable(
						"jade.mobgrowth.time",
						IThemeHelper.get().info(Component.translatable("jade.mobgrowth.paused"))));
				return;
			}
			int time = MobGrowthProvider.INSTANCE.decodeFromData(accessor).orElse(0);
			if (time > 0) {
				tooltip.add(Component.translatable("jade.mobgrowth.time", IThemeHelper.get().seconds(time, accessor.tickRate())));
			}
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_MOB_GROWTH;
		}
	}
}
