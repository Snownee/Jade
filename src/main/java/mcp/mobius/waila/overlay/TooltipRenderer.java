package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.Tooltip;
import mcp.mobius.waila.api.impl.Tooltip.Line;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigOverlay;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class TooltipRenderer {

    private final Minecraft client;
    private final Tooltip tooltip;
    private final boolean showItem;
    private final Size totalSize;
    ItemStack identifierStack;

    public TooltipRenderer(Tooltip tooltip, boolean showItem) {
        WailaTooltipEvent event = new WailaTooltipEvent(tooltip, DataAccessor.INSTANCE);
        MinecraftForge.EVENT_BUS.post(event);

        this.client = Minecraft.getInstance();
        this.showItem = showItem;
        this.tooltip = tooltip;

        totalSize = computeSize();
    }

    public Size computeSize() {
        int width = 0, height = 0;
        for (Line line : tooltip.lines) {
            Size size = line.getSize();
            width = Math.max(width, size.width);
            height += size.height;
        }
        width += hasItem() ? 30 : 10;
        height += 8;
        return new Size(width, height);
    }

    public void draw(MatrixStack matrixStack) {
        Rectangle position = getPosition();

        int x = position.x + (hasItem() ? 26 : 6);
        position.width += hasItem() ? 24 : 4;
        int y = position.y + 6;

        for (Line line : tooltip.lines) {
            line.render(matrixStack, x, y);
            //            RenderableTextComponent component = (RenderableTextComponent) line.getComponent();
            //            int xOffset = 0;
            //            for (RenderableTextComponent.RenderContainer container : component.getRenderers()) {
            //                Size size = container.getRenderer().getSize();
            //                container.getRenderer().render(matrixStack, x + xOffset, y);
            //                xOffset += size.width;
            //            }
            y += line.getSize().height;
        }
    }

    public Tooltip getTooltip() {
        return tooltip;
    }

    public boolean hasItem() {
        return showItem && Waila.CONFIG.get().getGeneral().shouldShowItem() && !RayTracing.INSTANCE.getIdentifierStack().isEmpty();
    }

    public Rectangle getPosition() {
        MainWindow window = Minecraft.getInstance().getMainWindow();
        ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
        int x = (int) (window.getScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()) - totalSize.width * overlay.tryFlip(overlay.getAnchorX()));
        int y = (int) (window.getScaledHeight() * (1.0F - overlay.getOverlayPosY()) - totalSize.height * overlay.getAnchorY());
        return new Rectangle(x, y, totalSize.width, totalSize.height);
    }

    public Size getSize() {
        return totalSize;
    }

}
