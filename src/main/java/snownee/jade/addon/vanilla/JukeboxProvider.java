package snownee.jade.addon.vanilla;

import com.mojang.serialization.MapCodec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.util.ServerDataUtil;

public enum JukeboxProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	private static final MapCodec<ItemStack> RECORD_CODEC = ItemStack.CODEC.fieldOf("record");

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		ItemStack stack = ItemStack.EMPTY;
		if (state.getValue(JukeboxBlock.HAS_RECORD)) {
			stack = ServerDataUtil.read(accessor.getServerData(), RECORD_CODEC).orElse(ItemStack.EMPTY);
		}
		if (!stack.isEmpty()) {
			Component name;
			if (stack.getItem() instanceof RecordItem record) {
				name = record.getDisplayName();
			} else {
				name = stack.getHoverName();
			}
			tooltip.add(Component.translatable("record.nowPlaying", IDisplayHelper.get().stripColor(name)));
		} else {
			tooltip.add(Component.translatable("tooltip.jade.empty"));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (accessor instanceof JukeboxBlockEntity jukebox) {
			ItemStack stack = jukebox.getTheItem();
			if (!stack.isEmpty()) {
				ServerDataUtil.write(data, RECORD_CODEC, stack);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_JUKEBOX;
	}
}
