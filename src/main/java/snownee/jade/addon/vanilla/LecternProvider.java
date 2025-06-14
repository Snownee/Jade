package snownee.jade.addon.vanilla;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;

public class LecternProvider implements StreamServerDataProvider<BlockAccessor, ItemStack> {
	public static final LecternProvider INSTANCE = new LecternProvider();

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		return accessor.getBlockState().getValue(LecternBlock.HAS_BOOK);
	}

	@Override
	public ItemStack streamData(BlockAccessor accessor) {
		return ((LecternBlockEntity) accessor.getBlockEntity()).getBook();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
		return ItemStack.OPTIONAL_STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_LECTERN;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			ItemStack stack = LecternProvider.INSTANCE.decodeFromData(accessor).orElse(ItemStack.EMPTY);
			if (stack.isEmpty()) {
				return;
			}
			tooltip.add(JadeUI.smallItem(stack));
			tooltip.append(JadeUI.text(IDisplayHelper.get().stripColor(stack.getHoverName()))
					.narration(Component.translatable("narration.jade.bookName", stack.getHoverName())));
		}

		@Override
		public ResourceLocation getUid() {
			return JadeIds.MC_LECTERN;
		}
	}
}
