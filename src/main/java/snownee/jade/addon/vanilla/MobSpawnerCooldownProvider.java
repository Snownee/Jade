package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class MobSpawnerCooldownProvider implements StreamServerDataProvider<BlockAccessor, Integer> {
	public static final MobSpawnerCooldownProvider INSTANCE = new MobSpawnerCooldownProvider();

	@Override
	public @Nullable Integer streamData(BlockAccessor accessor) {
		TrialSpawnerBlockEntity spawner = accessor.typedBlockEntity();
		TrialSpawnerStateData spawnerData = spawner.getTrialSpawner().getStateData();
		ServerLevel level = ((ServerLevel) accessor.getLevel());
		if (spawner.getTrialSpawner().canSpawnInLevel(level) && level.getGameTime() < spawnerData.cooldownEndsAt) {
			return (int) (spawnerData.cooldownEndsAt - level.getGameTime());
		}
		return null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_MOB_SPAWNER_COOLDOWN;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(JadeIds.MC_MOB_SPAWNER)) {
				return;
			}
			int cooldown = MobSpawnerCooldownProvider.INSTANCE.decodeFromData(accessor).orElse(0);
			tooltip.add(Component.translatable("jade.trial_spawner_cd", IThemeHelper.get().seconds(cooldown, accessor.tickRate())));
		}

		@Override
		public boolean isRequired() {
			return true;
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_MOB_SPAWNER_COOLDOWN;
		}
	}
}
