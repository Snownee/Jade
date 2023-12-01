package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobSpawnerProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	public static void appendTooltip(ITooltip tooltip, @Nullable Entity displayEntity, MutableComponent name) {
		if (displayEntity == null) {
			return;
		}
		name = Component.translatable("jade.spawner", name, displayEntity.getDisplayName());
		tooltip.replace(Identifiers.CORE_OBJECT_NAME, IThemeHelper.get().title(name));
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		MutableComponent name = accessor.getBlock().getName();
		Level level = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		if (accessor.getBlockEntity() instanceof SpawnerBlockEntity spawner) {
			appendTooltip(tooltip, spawner.getSpawner().getOrCreateDisplayEntity(level, pos), name);
		} else if (accessor.getBlockEntity() instanceof TrialSpawnerBlockEntity spawner) {
			TrialSpawnerData data = spawner.getTrialSpawner().getData();
			appendTooltip(tooltip, data.getOrCreateDisplayEntity(spawner.getTrialSpawner(), level, spawner.getState()), name);
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		MinecartSpawner spawner = (MinecartSpawner) accessor.getEntity();
		MutableComponent name = ObjectNameProvider.getEntityName(spawner).copy();
		appendTooltip(tooltip, spawner.getSpawner().getOrCreateDisplayEntity(accessor.getLevel(), accessor.getEntity().blockPosition()), name);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_MOB_SPAWNER;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.INSTANCE.getDefaultPriority() + 10;
	}

}
