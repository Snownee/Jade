package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import snownee.jade.JadePlugin;

public class BeehiveProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final BeehiveProvider INSTANCE = new BeehiveProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.BEEHIVE)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		int level = state.get(BeehiveBlock.HONEY_LEVEL); // 0~5
		tooltip.add(new TranslationTextComponent("jade.beehive.honey", new TranslationTextComponent("jade.fraction", level, 5).mergeStyle(level == 5 ? TextFormatting.GREEN : TextFormatting.WHITE)));
		if (accessor.getServerData().contains("Full")) {
			boolean full = accessor.getServerData().getBoolean("Full");
			int bees = accessor.getServerData().getByte("Bees");
			tooltip.add(new TranslationTextComponent("jade.beehive.bees", (full ? TextFormatting.GREEN : TextFormatting.WHITE) + "" + bees));
		}
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
		tag.keySet().clear();
		BeehiveTileEntity beehive = (BeehiveTileEntity) te;
		tag.putByte("Bees", (byte) beehive.getBeeCount());
		tag.putBoolean("Full", beehive.isFullOfBees());
	}

}
