package snownee.jade.addon.vanilla;

import java.util.List;
import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;

public class ShelfProvider implements StreamServerDataProvider<BlockAccessor, ItemStack> {
	public static final ShelfProvider INSTANCE = new ShelfProvider();

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		if (accessor.showDetails()) {
			return false;
		}
		OptionalInt slot = ((SelectableSlotContainer) accessor.getBlock()).getHitSlot(
				accessor.getHitResult(),
				accessor.getBlockState().getValue(HorizontalDirectionalBlock.FACING));
		if (slot.isEmpty()) {
			return false;
		}
		int i = slot.getAsInt();
		if (accessor.getBlock() instanceof ChiseledBookShelfBlock && i < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size() &&
				!accessor.getBlockState().getValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i))) {
			return false;
		}
		return true;
	}

	@Override
	public ItemStack streamData(BlockAccessor accessor) {
		int slot = ((SelectableSlotContainer) accessor.getBlock()).getHitSlot(accessor.getHitResult(), accessor.getSide()).orElse(-1);
		if (slot == -1) {
			return null;
		}
		return ((ListBackedContainer) accessor.getBlockEntity()).getItem(slot);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
		return ItemStack.OPTIONAL_STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_SHELF;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		private static ItemStack getHitBook(BlockAccessor accessor) {
			if (accessor.showDetails()) {
				return ItemStack.EMPTY;
			}
			return ShelfProvider.INSTANCE.decodeFromData(accessor).orElse(ItemStack.EMPTY);
		}

		@Override
		public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon) {
			ItemStack item = getHitBook(accessor);
			return item.isEmpty() ? null : JadeUI.item(item);
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
				TooltipDisplay tooltipDisplay = item.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
				item.addToTooltip(
						DataComponents.STORED_ENCHANTMENTS,
						Item.TooltipContext.of(accessor.getLevel()),
						tooltipDisplay,
						list::add,
						TooltipFlag.NORMAL);
				tooltip.addAll(list);
			}
		}

		@Override
		public int getDefaultPriority() {
			return ItemStorageProvider.BLOCK.getDefaultPriority() + 1;
		}

		@Override
		public ResourceLocation getUid() {
			return JadeIds.MC_SHELF;
		}
	}
}
