package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombieVillager;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum ZombieVillagerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("ConversionTime", Tag.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("ConversionTime");
		if (time > 0) {
			tooltip.add(Component.translatable("jade.zombieConversion.time", IThemeHelper.get().seconds(time)));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		ZombieVillager entity = (ZombieVillager) accessor.getEntity();
		if (entity.villagerConversionTime > 0) {
			tag.putInt("ConversionTime", entity.villagerConversionTime);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ZOMBIE_VILLAGER;
	}

}
