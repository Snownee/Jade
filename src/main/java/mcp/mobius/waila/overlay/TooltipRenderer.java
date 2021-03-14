package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IElement;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.Tooltip;
import mcp.mobius.waila.api.impl.Tooltip.Line;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigOverlay;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class TooltipRenderer {

    private final Tooltip tooltip;
    private final boolean showIcon;
    private final Size totalSize;
    IElement icon;

    public TooltipRenderer(Tooltip tooltip, boolean showIcon) {
        WailaTooltipEvent event = new WailaTooltipEvent(tooltip, DataAccessor.INSTANCE);
        MinecraftForge.EVENT_BUS.post(event);

        Minecraft.getInstance();
        this.showIcon = showIcon;
        this.tooltip = tooltip;
        if (showIcon) {
            icon = RayTracing.INSTANCE.getIcon();
        }

        totalSize = computeSize();
    }

    public Size computeSize() {
        int width = 0, height = 0;
        for (Line line : tooltip.lines) {
            Size size = line.getSize();
            width = Math.max(width, size.width);
            height += size.height;
        }
        if (hasIcon()) {
            Size size = icon.getCachedSize();
            width += 12 + size.width;
            height = Math.max(height, size.height - 5);
        } else {
            width += 10;
        }
        height += 8;
        return new Size(width, height);
    }

    public void draw(MatrixStack matrixStack) {
        Rectangle position = getPosition();

        int x = 6;
        if (hasIcon()) {
            x = icon.getCachedSize().width + 8;
        }
        int y = 6;

        for (Line line : tooltip.lines) {
            Size size = line.getSize();
            line.render(matrixStack, x, y, position.width, size.height);
            y += size.height;
        }
        position.width += x - 2;
    }

    public Tooltip getTooltip() {
        return tooltip;
    }

    public boolean hasIcon() {
        return showIcon && Waila.CONFIG.get().getGeneral().shouldShowIcon() && icon != null;
    }

    public Rectangle getPosition() {
        MainWindow window = Minecraft.getInstance().getMainWindow();
        ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
        //        int x = (int) (window.getScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()) - totalSize.width * overlay.tryFlip(overlay.getAnchorX()));
        //        int y = (int) (window.getScaledHeight() * (1.0F - overlay.getOverlayPosY()) - totalSize.height * overlay.getAnchorY());
        int x = (int) (window.getScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()));
        int y = (int) (window.getScaledHeight() * (1.0F - overlay.getOverlayPosY()));
        return new Rectangle(x, y, totalSize.width, totalSize.height);
    }

    public Size getSize() {
        return totalSize;
    }

}
