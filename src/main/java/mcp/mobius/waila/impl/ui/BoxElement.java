package mcp.mobius.waila.impl.ui;

import java.awt.Rectangle;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.TooltipRenderer;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoxElement extends Element {
	private final TooltipRenderer tooltip;
	@Nullable
	private final BorderStyle border;

	public BoxElement(Tooltip tooltip, BorderStyle border) {
		this.tooltip = new TooltipRenderer(tooltip, false);
		this.border = border;
	}

	@Override
	public Vec2 getSize() {
		if (tooltip.getTooltip().isEmpty()) {
			return Vec2.ZERO;
		}
		Vec2 size = tooltip.getSize();
		return new Vec2(size.x + 2, size.y + 4);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		if (tooltip.getTooltip().isEmpty()) {
			return;
		}
		Rectangle rect = tooltip.getPosition();
		RenderSystem.enableBlend();
		matrixStack.pushPose();
		matrixStack.translate(x, y, 0);
		if (border != null)
			DisplayHelper.INSTANCE.drawBorder(matrixStack, 0, 0, rect.width, rect.height, border);
		tooltip.draw(matrixStack);
		matrixStack.popPose();
	}

}
