package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class MobBreedingProvider implements StreamServerDataProvider<EntityAccessor, Integer> {
	public static final MobBreedingProvider INSTANCE = new MobBreedingProvider();
	private static final int IN_LOVE = -1;

	@Override
	public @Nullable Integer streamData(EntityAccessor accessor) {
		int time = 0;
		Entity entity = accessor.getEntity();
		if (entity instanceof Allay allay) {
			if (allay.duplicationCooldown > 0 && allay.duplicationCooldown < Integer.MAX_VALUE) {
				time = (int) allay.duplicationCooldown;
			}
		} else {
			Animal animal = (Animal) entity;
			if (animal.isInLove()) {
				return IN_LOVE;
			}
			time = animal.getAge();
		}
		return time > 0 ? time : null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_MOB_BREEDING;
	}

	public static class Client implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			int time = MobBreedingProvider.INSTANCE.decodeFromData(accessor).orElse(0);
			if (time == IN_LOVE) {
				tooltip.add(Component.translatable("jade.mobbreeding.fed"));
			} else if (time > 0) {
				tooltip.add(Component.translatable(
						accessor.getEntity() instanceof Allay ? "jade.mobduplication.time" : "jade.mobbreeding.time",
						IThemeHelper.get().seconds(time, accessor.tickRate())));
			}
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_MOB_BREEDING;
		}
	}
}
