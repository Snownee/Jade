package snownee.jade.renderers;

import java.awt.Dimension;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;

public class TTRenderSpan implements IWailaTooltipRenderer
{
    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor)
    {
        int width = Integer.valueOf(params[0]);
        int height = Integer.valueOf(params[1]);
        return new Dimension(width, height);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor)
    {
    }
}
