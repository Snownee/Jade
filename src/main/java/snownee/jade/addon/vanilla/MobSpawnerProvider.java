package snownee.jade.addon.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import snownee.jade.Jade;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

public enum MobSpawnerProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		SpawnerBlockEntity spawner = (SpawnerBlockEntity) accessor.getBlockEntity();
		MutableComponent name = accessor.getBlock().getName();
		appendTooltip(tooltip, accessor, spawner.getSpawner(), accessor.getPosition(), name);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		MinecartSpawner spawner = (MinecartSpawner) accessor.getEntity();
		MutableComponent name = ObjectNameProvider.getEntityName(spawner).copy();
		appendTooltip(tooltip, accessor, spawner.getSpawner(), spawner.blockPosition(), name);
	}

	public static void appendTooltip(ITooltip tooltip, Accessor<?> accessor, BaseSpawner spawner, BlockPos pos, MutableComponent name) {
		Entity entity = spawner.getOrCreateDisplayEntity(accessor.getLevel(), RandomSource.create(42), pos);
		//TODO multiple choices?
		if (entity != null) {
			name = Component.translatable("jade.spawner", name, entity.getDisplayName());
			tooltip.remove(Identifiers.CORE_OBJECT_NAME);
			tooltip.add(0, Jade.CONFIG.get().getFormatting().title(name), Identifiers.CORE_OBJECT_NAME);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_MOB_SPAWNER;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD - 99;
	}
}
