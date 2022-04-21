package snownee.jade.addon.forge;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import snownee.jade.Jade;
import snownee.jade.JadeCommonConfig;
import snownee.jade.VanillaPlugin;

public class InventoryProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final InventoryProvider INSTANCE = new InventoryProvider();
	// A set of tile names that need to be ignored in order to avoid network overload
	// Yay hardcoding, but it's better than nothing for now
	public static final Set<String> INVENTORY_IGNORE = Collections.synchronizedSet(Sets.newHashSet());

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.INVENTORY) || accessor.getBlockEntity() == null || accessor.getBlockEntity() instanceof AbstractFurnaceBlockEntity)
			return;

		append(tooltip, accessor);
	}

	public static void append(ITooltip tooltip, Accessor<?> accessor) {
		if (accessor.getServerData().getBoolean("Loot")) {
			tooltip.add(new TranslatableComponent("jade.not_generated"), VanillaPlugin.INVENTORY);
			return;
		}
		if (accessor.getServerData().getBoolean("Locked")) {
			tooltip.add(new TranslatableComponent("jade.locked"), VanillaPlugin.INVENTORY);
			return;
		}

		if (accessor.getServerData().contains("jadeHandler")) {
			ItemStackHandler itemHandler = new ItemStackHandler();
			itemHandler.deserializeNBT(accessor.getServerData().getCompound("jadeHandler"));

			int drawnCount = 0;
			int realSize = 0;
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if (!stack.isEmpty()) {
					++realSize;
				} else {
					break;
				}
			}
			boolean showName = realSize < 5;
			IElementHelper helper = tooltip.getElementHelper();
			List<IElement> elements = Lists.newArrayList();
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if (stack.isEmpty())
					break;
				if (i > 0 && (showName || drawnCount >= JadeCommonConfig.inventoryShowItemPreLine)) {
					tooltip.add(elements);
					elements.clear();
					drawnCount = 0;
				}

				if (showName) {
					ItemStack copy = stack.copy();
					copy.setCount(1);
					elements.add(Jade.smallItem(helper, copy).tag(VanillaPlugin.INVENTORY));
					elements.add(helper.text(new TextComponent(Integer.toString(stack.getCount())).append("× ").append(stack.getHoverName())).tag(VanillaPlugin.INVENTORY).message(null));
				} else {
					elements.add(helper.item(stack).tag(VanillaPlugin.INVENTORY));
				}
				drawnCount += 1;
			}

			if (!elements.isEmpty())
				tooltip.add(elements);
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean showDetails) {
		if (te == null || JadeCommonConfig.shouldIgnoreTE(tag.getString("id")) || te instanceof AbstractFurnaceBlockEntity) {
			return;
		}

		int size = showDetails ? JadeCommonConfig.inventoryDetailedShowAmount : JadeCommonConfig.inventoryNormalShowAmount;
		if (size == 0) {
			return;
		}

		if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null) {
			tag.putBoolean("Loot", true);
			return;
		}
		if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && te instanceof BaseContainerBlockEntity) {
			BaseContainerBlockEntity lockableBlockEntity = (BaseContainerBlockEntity) te;
			if (!lockableBlockEntity.canOpen(player)) {
				tag.putBoolean("Locked", true);
				return;
			}
		}

		IItemHandler itemHandler = null;
		LazyOptional<IItemHandler> optional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (optional.isPresent()) {
			itemHandler = optional.orElse(null);
		} else if (te instanceof Container) {
			itemHandler = new InvWrapper((Container) te);
		} else if (te instanceof EnderChestBlockEntity) {
			itemHandler = new InvWrapper(player.getEnderChestInventory());
		}
		putInvData(tag, itemHandler, size, 0);
	}

	public static void putInvData(CompoundTag tag, IItemHandler itemHandler, int size, int start) {
		if (itemHandler == null || size == 0) {
			return;
		}
		ItemStackHandler mergedHandler = new ItemStackHandler(size);
		boolean empty = true;
		int max = Math.min(itemHandler.getSlots(), start + size * 3);
		items:
		for (int i = start; i < max; i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (stack.hasTag() && stack.getTag().contains("CustomModelData")) {
				for (String key : stack.getTag().getAllKeys()) {
					if (key.toLowerCase(Locale.ENGLISH).endsWith("clear") && stack.getTag().getBoolean(key)) {
						continue items;
					}
				}
			}
			if (!stack.isEmpty()) {
				empty = false;
				ItemHandlerHelper.insertItemStacked(mergedHandler, stack.copy(), false);
				if (!mergedHandler.getStackInSlot(size - 1).isEmpty()) {
					break;
				}
			}
		}
		if (!empty) {
			tag.put("jadeHandler", mergedHandler.serializeNBT());
		}
	}

}
