package mcp.mobius.waila.overlay.element;

import java.awt.Rectangle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.api.ui.Size;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.ui.BorderStyle;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.TooltipRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoxElement extends Element {
	private final TooltipRenderer tooltip;
	private final BorderStyle border;

	public BoxElement(Tooltip tooltip, BorderStyle border) {
		this.tooltip = new TooltipRenderer(tooltip, false);
		this.border = border;
	}

	@Override
	public Size getSize() {
		if (tooltip.getTooltip().isEmpty()) {
			return Size.ZERO;
		}
		Size size = tooltip.getSize();
		return new Size(size.width + 2, size.height + 4);
	}

	@Override
	public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
		if (tooltip.getTooltip().isEmpty()) {
			return;
		}
		Rectangle rect = tooltip.getPosition();
		RenderSystem.enableBlend();
		matrixStack.push();
		matrixStack.translate(x, y, 0);
		DisplayHelper.INSTANCE.drawBorder(matrixStack, 0, 0, rect.width + 2, rect.height + 2, border);
		tooltip.draw(matrixStack);
		matrixStack.pop();
	}

}
