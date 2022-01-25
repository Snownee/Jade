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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class BreedingProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final BreedingProvider INSTANCE = new BreedingProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.MOB_BREEDING) || !accessor.getServerData().contains("BreedingCD", Tag.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("BreedingCD");
		if (time > 0) {
			tooltip.add(new TranslatableComponent("jade.mobbreeding.time", time / 20));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
		int time = ((Animal) entity).getAge();
		if (time > 0) {
			tag.putInt("BreedingCD", time);
		}
	}
}
