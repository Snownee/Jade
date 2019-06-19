package snownee.jade.renderers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.WeakHashMap;

import com.google.common.collect.Lists;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.config.OverlayConfig;
import mcp.mobius.waila.overlay.Tooltip;
import mcp.mobius.waila.utils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.config.Configuration;

public class TTRenderBorder implements IWailaTooltipRenderer
{
    private final WeakHashMap<String[], Tooltip> subTooltips = new WeakHashMap<String[], Tooltip>();

    private static Field FIELD_WIDTH;
    private static Field FIELD_HEIGHT;
    private static Field FIELD_TY;

    static
    {
        try
        {
            FIELD_WIDTH = Tooltip.class.getDeclaredField("w");
            FIELD_WIDTH.setAccessible(true);
            FIELD_HEIGHT = Tooltip.class.getDeclaredField("h");
            FIELD_HEIGHT.setAccessible(true);
            FIELD_TY = Tooltip.class.getDeclaredField("ty");
            FIELD_TY.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor)
    {
        Tooltip tooltip;
        if (subTooltips.containsKey(params))
        {
            tooltip = subTooltips.get(params);
        }
        else
        {
            tooltip = new Tooltip(Lists.newArrayList(params), false);
            subTooltips.put(params, tooltip);
        }
        int width, height;
        try
        {
            width = FIELD_WIDTH.getInt(tooltip);
            height = FIELD_HEIGHT.getInt(tooltip);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
            return new Dimension();
        }
        return new Dimension(width, height);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor)
    {
        Tooltip tooltip = subTooltips.get(params);
        if (tooltip != null)
        {
            int width, height, ty;
            try
            {
                width = FIELD_WIDTH.getInt(tooltip);
                height = FIELD_HEIGHT.getInt(tooltip);
                //ty = FIELD_TY.getInt(tooltip);
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
                return;
            }
            int offsetX = 6;
            Point pos = new Point(ConfigHandler.instance().getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_POSX, 0), ConfigHandler.instance().getConfig(Configuration.CATEGORY_GENERAL, Constants.CFG_WAILA_POSY, 0));
            ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
            int x = ((int) (resolution.getScaledWidth() / OverlayConfig.scale) - width - 1) * pos.x / 10000;
            int y = ((int) (resolution.getScaledHeight() / OverlayConfig.scale) - height - 1) * pos.y / 10000;
            GlStateManager.enableBlend();
            int color = Color.GRAY.getRGB();
            Gui.drawRect(0, 0, 1, height, color);
            Gui.drawRect(0, 0, width, 1, color);
            Gui.drawRect(width, 0, width + 1, height, color);
            Gui.drawRect(0, height, width + 1, height + 1, color);
            //GlStateManager.translate(-x - offsetX, -y - ty, 0);
            GlStateManager.translate(-x, -y, 0);
            tooltip.draw();
        }
    }

}
