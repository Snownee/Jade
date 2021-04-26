package mcp.mobius.waila.overlay.element;

import java.awt.Rectangle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.ui.BorderStyle;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.TooltipRenderer;
import net.minecraft.util.math.vector.Vector2f;
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
	public Vector2f getSize() {
		if (tooltip.getTooltip().isEmpty()) {
			return Vector2f.ZERO;
		}
		Vector2f size = tooltip.getSize();
		return new Vector2f(size.x + 2, size.y + 4);
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		if (tooltip.getTooltip().isEmpty()) {
			return;
		}
		Rectangle rect = tooltip.getPosition();
		RenderSystem.enableBlend();
		matrixStack.push();
		matrixStack.translate(x, y, 0);
		DisplayHelper.INSTANCE.drawBorder(matrixStack, 0, 0, rect.width, rect.height, border);
		tooltip.draw(matrixStack);
		matrixStack.pop();
	}

}
