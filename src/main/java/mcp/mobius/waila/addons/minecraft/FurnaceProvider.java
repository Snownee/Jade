package mcp.mobius.waila.addons.minecraft;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IElementHelper;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class FurnaceProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

    static final FurnaceProvider INSTANCE = new FurnaceProvider();

    @Override
    public void append(ITooltip tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (!config.get(MinecraftPlugin.CONFIG_DISPLAY_FURNACE))
            return;

        int progress = accessor.getServerData().getInt("progress");
        if (progress == 0)
            return;

        ListNBT furnaceItems = accessor.getServerData().getList("furnace", Constants.NBT.TAG_COMPOUND);
        NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
        for (int i = 0; i < furnaceItems.size(); i++)
            inventory.set(i, ItemStack.read(furnaceItems.getCompound(i)));

        IElementHelper helper = tooltip.getElementHelper();
        int total = accessor.getServerData().getInt("total");

        tooltip.add(helper.item(inventory.get(0)));
        tooltip.append(helper.item(inventory.get(1)));
        tooltip.append(helper.progress((float) progress / total));
        tooltip.append(helper.item(inventory.get(2)));
    }

    @Override
    public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity blockEntity) {
        AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) blockEntity;
        ListNBT items = new ListNBT();
        items.add(furnace.getStackInSlot(0).write(new CompoundNBT()));
        items.add(furnace.getStackInSlot(1).write(new CompoundNBT()));
        items.add(furnace.getStackInSlot(2).write(new CompoundNBT()));
        data.put("furnace", items);
        CompoundNBT furnaceTag = furnace.write(new CompoundNBT());
        data.putInt("progress", furnaceTag.getInt("CookTime")); // smh
        data.putInt("total", furnaceTag.getInt("CookTimeTotal")); // smh
    }

}
