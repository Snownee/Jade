package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import snownee.jade.VanillaPlugin;

public class ChickenEggProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final ChickenEggProvider INSTANCE = new ChickenEggProvider();

	@Override
	public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.CHICKEN_EGG) || !accessor.getServerData().contains("NextEgg")) {
			return;
		}
		tooltip.add(new TranslationTextComponent("jade.nextEgg", accessor.getServerData().getInt("NextEgg")));
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, Entity entity) {
		ChickenEntity chicken = (ChickenEntity) entity;
		tag.putInt("NextEgg", chicken.timeUntilNextEgg / 20);
	}

}
