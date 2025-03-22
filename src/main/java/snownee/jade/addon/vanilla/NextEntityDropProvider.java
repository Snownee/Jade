package snownee.jade.addon.vanilla;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import snownee.jade.api.Accessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum NextEntityDropProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		appendSeconds(tooltip, accessor, "NextEggIn", "jade.nextEgg");
		appendSeconds(tooltip, accessor, "NextScuteIn", "jade.nextScute");
		appendSeconds(tooltip, accessor, "NextSniffIn", "jade.nextSniff");
	}

	public static void appendSeconds(ITooltip tooltip, Accessor<?> accessor, String tagKey, String translationKey) {
		Optional<Integer> i = accessor.getServerData().getInt(tagKey);
		if (i.isEmpty()) {
			return;
		}
		tooltip.add(Component.translatable(translationKey, IThemeHelper.get().seconds(i.get(), accessor.tickRate())));
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
		} else if (accessor.getEntity() instanceof Sniffer sniffer) {
			long time = sniffer.getBrain().getTimeUntilExpiry(MemoryModuleType.SNIFF_COOLDOWN);
			if (time > 0 && time < max) {
				tag.putInt("NextSniffIn", (int) time);
			}
		}
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		if (accessor.getEntity() instanceof LivingEntity living) {
			return !living.isBaby();
		}
		return true;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_NEXT_ENTITY_DROP;
	}

}
