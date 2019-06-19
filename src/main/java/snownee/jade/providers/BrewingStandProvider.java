package snownee.jade.providers;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.Util;

public class BrewingStandProvider implements IWailaDataProvider
{
    public static final BrewingStandProvider INSTANCE = new BrewingStandProvider();

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (!config.getConfig("jade.brewingstand") || !accessor.getNBTData().hasKey("BrewingStand", Constants.NBT.TAG_COMPOUND))
        {
            return tooltip;
        }
        NBTTagCompound tag = accessor.getNBTData().getCompoundTag("BrewingStand");
        int fuel = tag.getInteger("fuel");
        String s = Util.wailaStack(new ItemStack(Items.BLAZE_POWDER)) + Util.offsetText(I18n.format("jade.brewingStand.fuel", fuel), 0, 4);
        int time = tag.getInteger("time");
        if (time > 0)
        {
            s += Util.span(5, 0) + Util.wailaStack(new ItemStack(Items.CLOCK)) + Util.offsetText(I18n.format("jade.brewingStand.time", time / 20), 0, 4);
        }
        tooltip.add(s);
        return tooltip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (player.isSneaking() && te instanceof TileEntityBrewingStand)
        {
            TileEntityBrewingStand brewingStand = (TileEntityBrewingStand) te;
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("time", brewingStand.getField(0));
            compound.setInteger("fuel", brewingStand.getField(1));
            tag.setTag("BrewingStand", compound);
        }
        return tag;
    }
}
