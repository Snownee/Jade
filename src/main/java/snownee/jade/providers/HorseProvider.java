package snownee.jade.providers;

import java.text.DecimalFormat;
import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.AbstractHorse;

public class HorseProvider implements IWailaEntityProvider
{
    public static final HorseProvider INSTANCE = new HorseProvider();
    private static DecimalFormat dfCommas = new DecimalFormat("##.##");

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        if (!accessor.getPlayer().isSneaking() || !config.getConfig("jade.horsestat"))
        {
            return currenttip;
        }
        AbstractHorse horse = (AbstractHorse) entity;
        double jumpStrength = horse.getHorseJumpStrength();
        double jumpHeight = -0.1817584952 * jumpStrength * jumpStrength * jumpStrength + 3.689713992 * jumpStrength * jumpStrength + 2.128599134 * jumpStrength - 0.343930367;
        IAttributeInstance iattributeinstance = horse.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        currenttip.add(I18n.format("jade.horseStat.jump", dfCommas.format(jumpHeight)));
        currenttip.add(I18n.format("jade.horseStat.speed", dfCommas.format(iattributeinstance.getAttributeValue())));
        return currenttip;
    }
}
