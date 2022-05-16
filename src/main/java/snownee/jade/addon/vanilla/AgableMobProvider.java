package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AgableMobProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final AgableMobProvider INSTANCE = new AgableMobProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.MOB_GROWTH) || !accessor.getServerData().contains("GrowingTime", Tag.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("GrowingTime");
		if (time < 0) {
			tooltip.add(new TranslatableComponent("jade.mobgrowth.time", (time * -1) / 20));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
		int time = ((AgeableMob) entity).getAge();
		if (time < 0) {
			tag.putInt("GrowingTime", time);
		}
	}

}
