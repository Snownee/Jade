package snownee.jade;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.jade.renderers.TTRenderBorder;
import snownee.jade.renderers.TTRenderSpan;
import snownee.jade.renderers.TTRenderStringX;

@SideOnly(Side.CLIENT)
@WailaPlugin
public class JadeClientPlugin implements IWailaPlugin
{

    @Override
    public void register(IWailaRegistrar registrar)
    {
        registrar.registerTooltipRenderer("jade.border", new TTRenderBorder());
        registrar.registerTooltipRenderer("jade.text", new TTRenderStringX());
        registrar.registerTooltipRenderer("jade.span", new TTRenderSpan());
    }

}
