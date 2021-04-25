package snownee.jade.addon.forge;

import java.util.Collections;
import java.util.List;
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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import snownee.jade.JadeCommonConfig;
import snownee.jade.VanillaPlugin;

public class InventoryProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final InventoryProvider INSTANCE = new InventoryProvider();
	// A set of tile names that need to be ignored in order to avoid network overload
	// Yay hardcoding, but it's better than nothing for now
	public static final Set<String> INVENTORY_IGNORE = Collections.synchronizedSet(Sets.newHashSet());

	@Override
	public void append(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.INVENTORY) || accessor.getTileEntity() == null || accessor.getTileEntity() instanceof AbstractFurnaceTileEntity)
			return;

		append(tooltip, accessor);
	}

	public static void append(ITooltip tooltip, Accessor accessor) {
		if (accessor.getServerData().contains("Locked") && accessor.getServerData().getBoolean("Locked")) {
			tooltip.add(new TranslationTextComponent("jade.locked"), VanillaPlugin.INVENTORY);
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

				elements.add(helper.item(stack).tag(VanillaPlugin.INVENTORY));
				if (showName) {
					elements.add(helper.text(stack.getDisplayName()).translate(new Vector2f(0, 4)).tag(VanillaPlugin.INVENTORY));
				}
				drawnCount += 1;
			}

			if (!elements.isEmpty())
				tooltip.add(elements);
		}
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
		if (te == null || JadeCommonConfig.shouldIgnoreTE(tag.getString("id")) || te instanceof AbstractFurnaceTileEntity) {
			return;
		}

		int size = player.isCrouching() ? JadeCommonConfig.inventorySneakShowAmount : JadeCommonConfig.inventoryNormalShowAmount;
		if (size == 0) {
			return;
		}

		if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && te instanceof LockableTileEntity) {
			LockableTileEntity lockableTileEntity = (LockableTileEntity) te;
			if (!lockableTileEntity.canOpen(player)) {
				tag.putBoolean("Locked", true);
				return;
			}
		}

		IItemHandler itemHandler = null;
		LazyOptional<IItemHandler> optional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (optional.isPresent()) {
			itemHandler = optional.orElse(null);
		} else if (te instanceof IInventory) {
			itemHandler = new InvWrapper((IInventory) te);
		} else if (te instanceof EnderChestTileEntity) {
			itemHandler = new InvWrapper(player.getInventoryEnderChest());
		}
		putInvData(tag, itemHandler, size, 0);
	}

	public static void putInvData(CompoundNBT tag, IItemHandler itemHandler, int size, int start) {
		if (itemHandler != null) {
			size = Math.min(size, itemHandler.getSlots());
			ItemStackHandler mergedHandler = new ItemStackHandler(size);
			boolean empty = true;
			for (int i = start; i < size; i++) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if (!stack.isEmpty()) {
					empty = false;
					ItemHandlerHelper.insertItemStacked(mergedHandler, stack.copy(), false);
				}
			}
			if (!empty) {
				tag.put("jadeHandler", mergedHandler.serializeNBT());
			}
		}
	}

}
