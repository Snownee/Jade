package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum ChickenEggProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("NextEgg")) {
			return;
		}
		tooltip.add(Component.translatable("jade.nextEgg", accessor.getServerData().getInt("NextEgg")));
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		Chicken chicken = (Chicken) accessor.getEntity();
		tag.putInt("NextEgg", chicken.eggTime / 20);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_CHICKEN_EGG;
	}

}
