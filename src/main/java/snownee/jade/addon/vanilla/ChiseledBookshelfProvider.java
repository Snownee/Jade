package snownee.jade.addon.vanilla;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ChiseledBookshelfProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	private static ItemStack getHitBook(BlockAccessor accessor) {
		if (accessor.showDetails() || !accessor.getServerData().contains("Book")) {
			return ItemStack.EMPTY;
		}
		return ItemStack.of(accessor.getServerData().getCompound("Book"));
	}

	@Override
	public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		ItemStack item = getHitBook(accessor);
		return item.isEmpty() ? null : IElementHelper.get().item(item);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		ItemStack item = getHitBook(accessor);
		if (item.isEmpty()) {
			return;
		}
		tooltip.remove(Identifiers.UNIVERSAL_ITEM_STORAGE);
		tooltip.add(IDisplayHelper.get().stripColor(item.getHoverName()));
		if (item.getTag() != null && item.getTag().contains(EnchantedBookItem.TAG_STORED_ENCHANTMENTS)) {
			List<Component> list = Lists.newArrayList();
			ItemStack.appendEnchantmentNames(list, EnchantedBookItem.getEnchantments(item));
			tooltip.addAll(list);
		}
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof ChiseledBookShelfBlockEntity bookshelf) {
			int slot = ((ChiseledBookShelfBlock) accessor.getBlock()).getHitSlot(accessor.getHitResult(), accessor.getBlockState())
					.orElse(-1);
			if (slot == -1) {
				return;
			}
			ItemStack book = bookshelf.getItem(slot);
			if (!book.isEmpty()) {
				data.put("Book", book.save(new CompoundTag()));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_CHISELED_BOOKSHELF;
	}

	@Override
	public int getDefaultPriority() {
		return ItemStorageProvider.getBlock().getDefaultPriority() + 1;
	}

}
