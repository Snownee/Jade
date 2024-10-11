package snownee.jade.addon.vanilla;

import java.util.List;
import java.util.OptionalInt;

import com.google.common.collect.Lists;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ChiseledBookshelfProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, ItemStack> {

	INSTANCE;

	private ItemStack getHitBook(BlockAccessor accessor) {
		if (accessor.showDetails()) {
			return ItemStack.EMPTY;
		}
		return decodeFromData(accessor).orElse(ItemStack.EMPTY);
	}

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		if (accessor.showDetails()) {
			return false;
		}
		OptionalInt slot = ((ChiseledBookShelfBlock) accessor.getBlock()).getHitSlot(accessor.getHitResult(), accessor.getBlockState());
		if (slot.isEmpty() || slot.getAsInt() >= ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size()) {
			return false;
		}
		return accessor.getBlockState().getValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot.getAsInt()));
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
		tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE);
		tooltip.add(IDisplayHelper.get().stripColor(item.getHoverName()));
		if (item.has(DataComponents.STORED_ENCHANTMENTS)) {
			List<Component> list = Lists.newArrayList();
			item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
					.addToTooltip(Item.TooltipContext.of(accessor.getLevel()), list::add, TooltipFlag.NORMAL);
			tooltip.addAll(list);
		}
	}

	@Override
	public ItemStack streamData(BlockAccessor accessor) {
		int slot = ((ChiseledBookShelfBlock) accessor.getBlock()).getHitSlot(accessor.getHitResult(), accessor.getBlockState()).orElse(-1);
		if (slot == -1) {
			return null;
		}
		return ((ChiseledBookShelfBlockEntity) accessor.getBlockEntity()).getItem(slot);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
		return ItemStack.OPTIONAL_STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_CHISELED_BOOKSHELF;
	}

	@Override
	public int getDefaultPriority() {
		return ItemStorageProvider.getBlock().getDefaultPriority() + 1;
	}

}
