package snownee.jade.addon.vanilla;

import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public class JukeboxProvider implements StreamServerDataProvider<BlockAccessor, ItemStack> {
	public static final JukeboxProvider INSTANCE = new JukeboxProvider();

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		return accessor.getBlockState().getValue(JukeboxBlock.HAS_RECORD);
	}

	@Override
	public ItemStack streamData(BlockAccessor accessor) {
		return accessor.<JukeboxBlockEntity>typedBlockEntity().getTheItem();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
		return ItemStack.OPTIONAL_STREAM_CODEC;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_JUKEBOX;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			Optional<ItemStack> result = JukeboxProvider.INSTANCE.decodeFromData(accessor);
			if (result.isEmpty()) {
				return;
			}
			ItemStack stack = result.get();
			if (stack.isEmpty()) {
				tooltip.add(Component.translatable("tooltip.jade.empty"));
				return;
			}
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
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_JUKEBOX;
		}
	}
}
