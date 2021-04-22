package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.JadePlugin;

public class AgeableEntityProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final AgeableEntityProvider INSTANCE = new AgeableEntityProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.MOB_GROWTH) || !accessor.getServerData().contains("GrowingTime", Constants.NBT.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("GrowingTime");
		if (time < 0) {
			tooltip.add(new TranslationTextComponent("jade.mobgrowth.time", (time * -1) / 20));
		}
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, Entity entity) {
		int time = ((AgeableEntity) entity).getGrowingAge();
		if (time < 0) {
			tag.putInt("GrowingTime", time);
		}
	}

}
