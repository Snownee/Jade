package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class ZombieVillagerProvider implements StreamServerDataProvider<EntityAccessor, Integer> {
	public static final ZombieVillagerProvider INSTANCE = new ZombieVillagerProvider();

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		return ((ZombieVillager) accessor.getEntity()).isConverting();
	}

	@Override
	public @Nullable Integer streamData(EntityAccessor accessor) {
		int time = ((ZombieVillager) accessor.getEntity()).villagerConversionTime;
		return time > 0 ? time : null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ZOMBIE_VILLAGER;
	}

	public static class Client implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			int time = ZombieVillagerProvider.INSTANCE.decodeFromData(accessor).orElse(0);
			if (time > 0) {
				tooltip.add(Component.translatable("jade.zombieConversion.time", IThemeHelper.get().seconds(time, accessor.tickRate())));
			}
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_ZOMBIE_VILLAGER;
		}
	}
}
