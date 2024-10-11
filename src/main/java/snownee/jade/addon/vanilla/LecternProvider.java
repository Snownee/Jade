package snownee.jade.addon.vanilla;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import snownee.jade.api.ui.IElementHelper;

public enum LecternProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, ItemStack> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		ItemStack stack = decodeFromData(accessor).orElse(ItemStack.EMPTY);
		if (stack.isEmpty()) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		tooltip.add(helper.smallItem(stack));
		tooltip.append(helper.text(IDisplayHelper.get().stripColor(stack.getHoverName()))
				.message(I18n.get("narration.jade.bookName", stack.getHoverName().getString())));
	}

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

}
