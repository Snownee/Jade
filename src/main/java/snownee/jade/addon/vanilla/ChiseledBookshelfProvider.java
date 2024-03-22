package snownee.jade.addon.vanilla;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
import snownee.jade.util.ServerDataUtil;

public enum ChiseledBookshelfProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	public static final MapCodec<ItemStack> BOOK_CODEC = ItemStack.CODEC.fieldOf("book");

	private static ItemStack getHitBook(BlockAccessor accessor) {
		if (accessor.showDetails()) {
			return ItemStack.EMPTY;
		}
		Optional<ItemStack> result = ServerDataUtil.read(accessor.getServerData(), BOOK_CODEC);
		return result.orElse(ItemStack.EMPTY);
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
		if (item.has(DataComponents.STORED_ENCHANTMENTS)) {
			List<Component> list = Lists.newArrayList();
			item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).addToTooltip(list::add, TooltipFlag.NORMAL);
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
				ServerDataUtil.write(data, BOOK_CODEC, book);
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
