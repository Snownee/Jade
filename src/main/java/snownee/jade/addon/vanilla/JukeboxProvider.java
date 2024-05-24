package snownee.jade.addon.vanilla;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public enum JukeboxProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	private static final MapCodec<ItemStack> RECORD_CODEC = ItemStack.CODEC.fieldOf("record");

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		ItemStack stack = ItemStack.EMPTY;
		if (state.getValue(JukeboxBlock.HAS_RECORD)) {
			stack = accessor.readData(RECORD_CODEC).orElse(ItemStack.EMPTY);
		}
		if (!stack.isEmpty()) {
			Component name;
			JukeboxPlayable playable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
			if (playable != null) {
				name = playable.song()
						.unwrap(accessor.getLevel().registryAccess())
						.map($ -> $.value().description())
						.orElse(stack.getHoverName());
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
		if (accessor.getBlockEntity() instanceof JukeboxBlockEntity jukebox) {
			ItemStack stack = jukebox.getTheItem();
			if (!stack.isEmpty()) {
				accessor.writeData(RECORD_CODEC, stack);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_JUKEBOX;
	}
}
