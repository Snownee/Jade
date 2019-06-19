package snownee.jade.providers;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityLlama;

public class ChestHorseProvider implements IWailaEntityProvider
{
    public static final ChestHorseProvider INSTANCE = new ChestHorseProvider();

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        if (!accessor.getPlayer().isSneaking() || !config.getConfig("jade.horsechest"))
        {
            return currenttip;
        }
        AbstractChestHorse horse = (AbstractChestHorse) entity;
        if (horse instanceof EntityLlama)
        {
            currenttip.add(I18n.format("jade.llamaStrength", ((EntityLlama) horse).getStrength()));
        }
        return currenttip;
    }
}
