package snownee.jade.providers;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class AgeableEntityProvider implements IWailaEntityProvider
{
    public static final AgeableEntityProvider INSTANCE = new AgeableEntityProvider();

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        if (!accessor.getPlayer().isSneaking() || !config.getConfig("jade.mobgrowth") || !accessor.getNBTData().hasKey("GrowingTime", Constants.NBT.TAG_INT))
        {
            return currenttip;
        }
        int time = accessor.getNBTData().getInteger("GrowingTime");
        if (time < 0)
        {
            currenttip.add(I18n.format("jade.mobgrowth.time", (time * -1) / 20));
        }
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity entity, NBTTagCompound tag, World world)
    {
        if (player.isSneaking())
        {
            int time = ((EntityAgeable) entity).getGrowingAge();
            if (time < 0)
            {
                tag.setInteger("GrowingTime", time);
            }
        }
        return tag;
    }
}
