package snownee.jade.renderers;

import java.awt.Dimension;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.config.OverlayConfig;
import mcp.mobius.waila.overlay.DisplayUtil;

public class TTRenderStringX implements IWailaTooltipRenderer
{
    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor)
    {
        int ox = Integer.valueOf(params[1]);
        int oy = Integer.valueOf(params[2]);
        return new Dimension(ox + DisplayUtil.getDisplayWidth(params[0]), oy + (params[0].equals("") ? 0 : 8));
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor)
    {
        int ox = Integer.valueOf(params[1]);
        int oy = Integer.valueOf(params[2]);
        DisplayUtil.drawString(params[0], ox, oy, OverlayConfig.fontcolor, true);
    }
}
