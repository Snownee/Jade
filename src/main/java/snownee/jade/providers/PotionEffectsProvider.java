package snownee.jade.providers;

import java.util.Collection;
import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionEffectsProvider implements IWailaEntityProvider
{
    public static final PotionEffectsProvider INSTANCE = new PotionEffectsProvider();

    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        if (!accessor.getPlayer().isSneaking() || !config.getConfig("jade.potioneffects") || !accessor.getNBTData().hasKey("Potions"))
        {
            return currenttip;
        }
        NBTTagList list = accessor.getNBTData().getTagList("Potions", Constants.NBT.TAG_COMPOUND);
        String[] lines = new String[list.tagCount()];
        for (int i = 0; i < lines.length; i++)
        {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            int duration = compound.getInteger("Duration");
            String name = I18n.format(compound.getString("Name"));
            String amplifier = I18n.format("potion.potency." + compound.getInteger("Amplifier"));
            String s = I18n.format("jade.potion", name, amplifier, getPotionDurationString(duration));
            lines[i] = (compound.getBoolean("Bad") ? TextFormatting.RED : TextFormatting.GREEN) + s;
        }
        currenttip.add(SpecialChars.getRenderString("jade.border", lines));
        return currenttip;
    }

    public static String getPotionDurationString(int duration)
    {
        if (duration == 32767)
        {
            return "**:**";
        }
        else
        {
            int i = MathHelper.floor(duration);
            return ticksToElapsedTime(i);
        }
    }

    public static String ticksToElapsedTime(int ticks)
    {
        int i = ticks / 20;
        int j = i / 60;
        i = i % 60;
        return i < 10 ? j + ":0" + i : j + ":" + i;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity entity, NBTTagCompound tag, World world)
    {
        if (!player.isSneaking())
        {
            return tag;
        }
        NBTTagList list = new NBTTagList();
        EntityLivingBase living = (EntityLivingBase) entity;
        Collection<PotionEffect> effects = living.getActivePotionEffects();
        if (effects.isEmpty())
        {
            return tag;
        }
        for (PotionEffect effect : effects)
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("Name", effect.getEffectName());
            compound.setInteger("Amplifier", effect.getAmplifier());
            compound.setInteger("Duration", effect.getDuration());
            compound.setBoolean("Bad", effect.getPotion().isBadEffect());
            list.appendTag(compound);
        }
        tag.setTag("Potions", list);
        return tag;
    }
}
