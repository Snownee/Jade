package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.VanillaPlugin;

public class BreedingProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final BreedingProvider INSTANCE = new BreedingProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.MOB_BREEDING) || !accessor.getServerData().contains("BreedingCD", Constants.NBT.TAG_INT)) {
			return;
		}
		int time = accessor.getServerData().getInt("BreedingCD");
		if (time > 0) {
			tooltip.add(new TranslationTextComponent("jade.mobbreeding.time", TextFormatting.WHITE.toString() + time / 20));
		}
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, Entity entity, boolean showDetails) {
		int time = ((AnimalEntity) entity).getGrowingAge();
		if (time > 0) {
			tag.putInt("BreedingCD", time);
		}
	}
}
