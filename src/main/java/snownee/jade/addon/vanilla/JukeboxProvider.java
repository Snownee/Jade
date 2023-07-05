package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public enum JukeboxProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.getValue(JukeboxBlock.HAS_RECORD) && accessor.getServerData().contains("Record")) {
			try {
				ItemStack stack = ItemStack.of(accessor.getServerData().getCompound("Record"));
				Component name;
				if (stack.getItem() instanceof RecordItem record) {
					name = record.getDisplayName();
				} else {
					name = stack.getHoverName();
				}
				tooltip.add(new TranslatableComponent("record.nowPlaying", IDisplayHelper.get().stripColor(name)));
			} catch (Exception e) {
				Jade.LOGGER.catching(e);
			}
		} else {
			tooltip.add(new TranslatableComponent("tooltip.jade.empty"));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity blockEntity, boolean showDetails) {
		if (blockEntity instanceof JukeboxBlockEntity jukebox) {
			ItemStack stack = jukebox.getRecord();
			if (!stack.isEmpty()) {
				data.put("Record", stack.save(new CompoundTag()));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_JUKEBOX;
	}
}
