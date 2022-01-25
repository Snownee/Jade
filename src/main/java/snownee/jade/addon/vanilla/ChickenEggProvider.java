package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class ChickenEggProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final ChickenEggProvider INSTANCE = new ChickenEggProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.CHICKEN_EGG) || !accessor.getServerData().contains("NextEgg")) {
			return;
		}
		tooltip.add(new TranslatableComponent("jade.nextEgg", accessor.getServerData().getInt("NextEgg")));
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
		Chicken chicken = (Chicken) entity;
		tag.putInt("NextEgg", chicken.eggTime / 20);
	}

}
