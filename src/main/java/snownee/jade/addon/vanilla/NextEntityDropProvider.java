package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import snownee.jade.api.Accessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum NextEntityDropProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		appendSeconds(tooltip, accessor, "NextEggIn", "jade.nextEgg");
		appendSeconds(tooltip, accessor, "NextScuteIn", "jade.nextScute");
	}

	public static void appendSeconds(ITooltip tooltip, Accessor<?> accessor, String tagKey, String translationKey) {
		if (accessor.getServerData().contains(tagKey)) {
			tooltip.add(Component.translatable(
					translationKey,
					IThemeHelper.get().seconds(accessor.getServerData().getInt(tagKey), accessor.tickRate())));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		int max = 24000 * 2;
		if (accessor.getEntity() instanceof Chicken chicken) {
			if (!chicken.isBaby() && chicken.eggTime < max) {
				tag.putInt("NextEggIn", chicken.eggTime);
			}
		} else if (accessor.getEntity() instanceof Armadillo armadillo) {
			if (!armadillo.isBaby() && armadillo.scuteTime < max) {
				tag.putInt("NextScuteIn", armadillo.scuteTime);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_NEXT_ENTITY_DROP;
	}

}
