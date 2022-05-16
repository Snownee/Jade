package snownee.jade.addon.vanilla;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class BeehiveProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final BeehiveProvider INSTANCE = new BeehiveProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.BEEHIVE)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		int level = state.getValue(BeehiveBlock.HONEY_LEVEL); // 0~5
		tooltip.add(new TranslatableComponent("jade.beehive.honey", new TranslatableComponent("jade.fraction", level, 5).withStyle(level == 5 ? ChatFormatting.GREEN : ChatFormatting.WHITE)));
		if (accessor.getServerData().contains("Full")) {
			boolean full = accessor.getServerData().getBoolean("Full");
			int bees = accessor.getServerData().getByte("Bees");
			tooltip.add(new TranslatableComponent("jade.beehive.bees", (full ? ChatFormatting.GREEN.toString() : "") + bees));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean showDetails) {
		tag.getAllKeys().clear();
		BeehiveBlockEntity beehive = (BeehiveBlockEntity) te;
		tag.putByte("Bees", (byte) beehive.getOccupantCount());
		tag.putBoolean("Full", beehive.isFull());
	}

}
