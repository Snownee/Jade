package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobSpawnerCooldownProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (config.get(JadeIds.MC_MOB_SPAWNER) && accessor.getServerData().contains("Cooldown")) {
			tooltip.add(Component.translatable(
					"jade.trial_spawner_cd",
					IThemeHelper.get().seconds(accessor.getServerData().getInt("Cooldown"), accessor.tickRate())));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof TrialSpawnerBlockEntity spawner) {
			TrialSpawnerData spawnerData = spawner.getTrialSpawner().getData();
			Level level = accessor.getLevel();
			if (spawner.getTrialSpawner().canSpawnInLevel(level) && level.getGameTime() < spawnerData.cooldownEndsAt) {
				data.putInt("Cooldown", (int) (spawnerData.cooldownEndsAt - level.getGameTime()));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_MOB_SPAWNER_COOLDOWN;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
