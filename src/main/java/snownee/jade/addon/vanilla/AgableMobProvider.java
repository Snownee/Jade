package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import snownee.jade.VanillaPlugin;

public class AgableMobProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final AgableMobProvider INSTANCE = new AgableMobProvider();

	@Override
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
