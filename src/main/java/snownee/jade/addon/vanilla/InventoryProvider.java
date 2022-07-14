package snownee.jade.addon.vanilla;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.RenderableTextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import snownee.jade.JadeCommonConfig;
import snownee.jade.JadePlugin;
import snownee.jade.Renderables;

public class InventoryProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final InventoryProvider INSTANCE = new InventoryProvider();
	// A set of tile names that need to be ignored in order to avoid network overload
	// Yay hardcoding, but it's better than nothing for now
	public static final Set<String> INVENTORY_IGNORE = Collections.synchronizedSet(Sets.newHashSet());

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.INVENTORY) || accessor.getTileEntity() == null || accessor.getTileEntity() instanceof AbstractFurnaceTileEntity)
			return;

		if (accessor.getServerData().getBoolean("Loot")) {
			tooltip.add(new TranslationTextComponent("jade.not_generated"));
			return;
		}

		if (accessor.getServerData().contains("Locked") && accessor.getServerData().getBoolean("Locked")) {
			tooltip.add(new TranslationTextComponent("jade.locked"));
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
			List<RenderableTextComponent> components = Lists.newArrayList();
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if (stack.isEmpty())
					break;
				if (i > 0 && (showName || drawnCount >= JadeCommonConfig.inventoryShowItemPreLine)) {
					tooltip.add(Renderables.of(components.toArray(new RenderableTextComponent[0])));
					components.clear();
					drawnCount = 0;
				}

				components.add(Renderables.item(stack));
				if (showName) {
					components.add(Renderables.offsetText(stack.getDisplayName(), 0, 4));
				}
				drawnCount += 1;
			}

			if (!components.isEmpty())
				tooltip.add(Renderables.of(components.toArray(new RenderableTextComponent[0])));
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

		if (te instanceof LockableLootTileEntity && ((LockableLootTileEntity) te).lootTable != null) {
			tag.putBoolean("Loot", true);
			return;
		}

		if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && te instanceof LockableTileEntity) {
			LockableTileEntity lockableTileEntity = (LockableTileEntity) te;
			if (lockableTileEntity.code != LockCode.EMPTY_CODE) {
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
		if (itemHandler != null) {
			size = Math.min(size, itemHandler.getSlots());
			ItemStackHandler mergedHandler = new ItemStackHandler(size);
			boolean empty = true;
			for (int i = 0; i < size; i++) {
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
