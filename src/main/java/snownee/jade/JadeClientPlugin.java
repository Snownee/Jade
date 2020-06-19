package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import snownee.jade.client.renderer.BoxTooltipRenderer;
import snownee.jade.client.renderer.StringTooltipRenderer;

@WailaPlugin
public class JadeClientPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            registrar.registerTooltipRenderer(Renderables.BORDER, new BoxTooltipRenderer());
            registrar.registerTooltipRenderer(Renderables.OFFSET_TEXT, new StringTooltipRenderer());
        });
    }

}
