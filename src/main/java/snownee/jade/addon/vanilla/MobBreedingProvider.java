package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum MobBreedingProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("BreedingCD", Tag.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("BreedingCD");
		if (time > 0) {
			tooltip.add(Component.translatable("jade.mobbreeding.time", time / 20));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
		int time = ((Animal) entity).getAge();
		if (time > 0) {
			tag.putInt("BreedingCD", time);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_MOB_BREEDING;
	}
}
