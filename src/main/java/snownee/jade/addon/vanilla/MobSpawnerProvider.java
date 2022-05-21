package snownee.jade.addon.vanilla;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum MobSpawnerProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		SpawnerBlockEntity spawner = (SpawnerBlockEntity) accessor.getBlockEntity();
		String name = I18n.get(accessor.getBlock().getDescriptionId());
		Entity entity = spawner.getSpawner().getOrCreateDisplayEntity(accessor.getLevel());
		//TODO multiple choices?
		if (entity != null) {
			name = I18n.get("jade.spawner", name, entity.getDisplayName().getString());
			tooltip.remove(Identifiers.CORE_OBJECT_NAME);
			tooltip.add(Jade.CONFIG.get().getFormatting().title(name), Identifiers.CORE_OBJECT_NAME);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_MOB_SPAWNER;
	}
}
