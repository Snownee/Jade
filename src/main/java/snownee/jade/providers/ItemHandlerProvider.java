package snownee.jade.providers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.utils.InventoryUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import snownee.jade.ModConfig;
import snownee.jade.Util;

public class ItemHandlerProvider implements IWailaDataProvider
{

    public static final ItemHandlerProvider INSTANCE = new ItemHandlerProvider();
    // A set of tile names that need to be ignored in order to avoid network overload
    // Yay hardcoding, but it's better than nothing for now
    public static Set<String> INVENTORY_IGNORE = Collections.EMPTY_SET;

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (!config.getConfig("capability.inventoryinfo") || accessor.getTileEntity() == null || accessor.getTileEntity().getClass() == TileEntityFurnace.class)
            return currenttip;

        if (accessor.getNBTData().hasKey("locked") && accessor.getNBTData().getBoolean("locked"))
        {
            currenttip.add(I18n.format("jade.locked"));
        }
        else if (accessor.getNBTData().hasKey("handler"))
        {
            int handlerSize = accessor.getNBTData().getInteger("handlerSize");
            ItemStackHandler itemHandler = new ItemStackHandler();
            itemHandler.setSize(handlerSize);
            InventoryUtils.populateInv(itemHandler, accessor.getNBTData().getTagList("handler", Constants.NBT.TAG_COMPOUND));

            String renderString = "";
            int drawnCount = 0;
            int realSize = 0;
            for (int i = 0; i < itemHandler.getSlots(); i++)
            {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty())
                {
                    ++realSize;
                }
            }
            boolean showName = realSize < 5;
            for (int i = 0; i < itemHandler.getSlots(); i++)
            {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (stack.isEmpty())
                    continue;
                if (i > 0 && (showName || drawnCount >= ModConfig.inventoryShowItemPreLine))
                {
                    currenttip.add(renderString);
                    renderString = "";
                    drawnCount = 0;
                }

                renderString += Util.wailaStack(stack);
                if (showName)
                {
                    renderString += Util.offsetText(TextFormatting.WHITE + stack.getDisplayName(), 0, 4);
                }
                drawnCount += 1;
            }

            if (!Strings.isNullOrEmpty(renderString))
                ((ITaggedList<String, String>) currenttip).add(renderString, "IItemHandler");
        }
        return currenttip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (te != null && !INVENTORY_IGNORE.contains(tag.getString("id")) && te.getClass() != TileEntityFurnace.class)
        {
            if ((player.isSneaking() ? ModConfig.inventorySneakShowAmount : ModConfig.inventoryNormalShowAmount) == 0)
            {
                return tag;
            }
            tag.removeTag("Items"); // Should catch all inventories that do things the standard way. Keeps from duplicating the item list and doubling the packet size
            IItemHandler itemHandler = null;
            if (!ModConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && te instanceof ILockableContainer)
            {
                ILockableContainer ilockablecontainer = (ILockableContainer) te;
                if (ilockablecontainer.isLocked() && !player.canOpen(ilockablecontainer.getLockCode()))
                {
                    tag.setBoolean("locked", true);
                    return tag;
                }
            }
            else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            }
            else if (te instanceof IInventory)
            {
                itemHandler = new InvWrapper((IInventory) te);
            }
            else if (te instanceof TileEntityEnderChest)
            {
                itemHandler = new InvWrapper(player.getInventoryEnderChest());
            }
            if (itemHandler != null)
            {
                int size = player.isSneaking() ? ModConfig.inventorySneakShowAmount : ModConfig.inventoryNormalShowAmount;
                tag.setTag("handler", InventoryUtils.invToNBT(itemHandler, size));
                tag.setInteger("handlerSize", size);
            }
        }

        return tag;
    }

}
