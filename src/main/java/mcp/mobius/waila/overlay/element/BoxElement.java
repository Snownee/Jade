package mcp.mobius.waila.overlay.element;

import java.awt.Color;
import java.awt.Rectangle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.api.impl.Tooltip;
import mcp.mobius.waila.overlay.TooltipRenderer;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoxElement extends Element {
    private final TooltipRenderer tooltip;

    public BoxElement(Tooltip tooltip) {
        this.tooltip = new TooltipRenderer(tooltip, false);
    }

    @Override
    public Size getSize() {
        Size size = tooltip.getSize();
        return new Size(size.width + 2, size.height + 4);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        Rectangle rect = tooltip.getPosition();
        RenderSystem.enableBlend();
        int color = Color.GRAY.getRGB();
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        AbstractGui.fill(matrixStack, 0, 0, 1, rect.height, color);
        AbstractGui.fill(matrixStack, 0, 0, rect.width, 1, color);
        AbstractGui.fill(matrixStack, rect.width, 0, rect.width + 1, rect.height, color);
        AbstractGui.fill(matrixStack, 0, rect.height, rect.width + 1, rect.height + 1, color);
        matrixStack.translate(-rect.x, -rect.y, 0);
        tooltip.draw(matrixStack);
        matrixStack.pop();
    }

}
