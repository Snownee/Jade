package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.client.renderer.BoxTooltipRenderer;
import snownee.jade.client.renderer.StringTooltipRenderer;

@OnlyIn(Dist.CLIENT)
@WailaPlugin
public class JadeClientPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerTooltipRenderer(Renderables.BORDER, new BoxTooltipRenderer());
        registrar.registerTooltipRenderer(Renderables.OFFSET_TEXT, new StringTooltipRenderer());
    }

}
