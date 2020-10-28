package mcp.mobius.waila.overlay.tooltiprenderers;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.awt.Dimension;

public class TooltipRendererProgressBar implements ITooltipRenderer {

    private static final ResourceLocation SHEET = new ResourceLocation(Waila.MODID, "textures/sprites.png");

    @Override
    public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
        return new Dimension(26, 16);
    }

    @Override
    public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
        int currentValue = tag.getInt("progress");

        Minecraft.getInstance().getTextureManager().bindTexture(SHEET);

        // Draws the "empty" background arrow
        DisplayUtil.drawTexturedModalRect(x + 2, y, 0, 16, 22, 16, 22, 16);

        int maxValue = tag.getInt("total");
        if (maxValue > 0) {
            int progress = (currentValue * 22) / maxValue;
            // Draws the "full" foreground arrow based on the progress
            DisplayUtil.drawTexturedModalRect(x + 2, y, 0, 0, progress + 1, 16, progress + 1, 16);
        }
    }
}
