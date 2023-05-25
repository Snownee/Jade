package snownee.jade.addon.vanilla;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum BeehiveProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		int level = state.getValue(BeehiveBlock.HONEY_LEVEL); // 0~5
		tooltip.add(Component.translatable("jade.beehive.honey", Component.translatable("jade.fraction", level, 5).withStyle(level == 5 ? ChatFormatting.GREEN : ChatFormatting.WHITE)));
		if (accessor.getServerData().contains("Full")) {
			boolean full = accessor.getServerData().getBoolean("Full");
			int bees = accessor.getServerData().getByte("Bees");
			tooltip.add(Component.translatable("jade.beehive.bees", (full ? ChatFormatting.GREEN.toString() : "") + bees));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor) {
		tag.getAllKeys().clear();
		BeehiveBlockEntity beehive = (BeehiveBlockEntity) blockAccessor.getBlockEntity();
		tag.putByte("Bees", (byte) beehive.getOccupantCount());
		tag.putBoolean("Full", beehive.isFull());
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_BEEHIVE;
	}

}
