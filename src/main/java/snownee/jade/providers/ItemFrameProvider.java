package snownee.jade.providers;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.jade.Util;

public class ItemFrameProvider implements IWailaEntityProvider
{
    public static final ItemFrameProvider INSTANCE = new ItemFrameProvider();

    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        if (!config.getConfig("jade.itemframe"))
        {
            return currenttip;
        }
        EntityItemFrame itemFrame = (EntityItemFrame) entity;
        ItemStack stack = itemFrame.getDisplayedItem();
        if (stack.isEmpty())
        {
            return currenttip;
        }
        currenttip.add(Util.wailaStack(stack) + Util.offsetText(TextFormatting.WHITE + stack.getDisplayName(), 0, 4));
        return currenttip;
    }
}
