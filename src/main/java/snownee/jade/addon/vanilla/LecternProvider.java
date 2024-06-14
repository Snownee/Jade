package snownee.jade.addon.vanilla;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;

public enum LecternProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (!state.getValue(LecternBlock.HAS_BOOK)) {
			return;
		}
		ItemStack stack = accessor.readData(ChiseledBookshelfProvider.BOOK_CODEC).orElse(ItemStack.EMPTY);
		if (!stack.isEmpty()) {
			IElementHelper helper = IElementHelper.get();
			tooltip.add(helper.smallItem(stack));
			tooltip.append(helper.text(IDisplayHelper.get().stripColor(stack.getHoverName()))
					.message(I18n.get("narration.jade.bookName", stack.getHoverName().getString())));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof LecternBlockEntity lectern) {
			ItemStack stack = lectern.getBook();
			if (!stack.isEmpty()) {
				if (stack.has(DataComponents.CUSTOM_NAME) || stack.getItem() != Items.WRITABLE_BOOK) {
					accessor.writeData(ChiseledBookshelfProvider.BOOK_CODEC, stack);
				}
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_LECTERN;
	}

}
