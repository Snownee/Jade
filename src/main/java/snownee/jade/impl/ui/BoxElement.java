package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.Tooltip;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.TooltipRenderer;

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
		Rect2i rect = tooltip.getPosition();
		RenderSystem.enableBlend();
		matrixStack.pushPose();
		matrixStack.translate(x, y, 0);
		if (border != null)
			DisplayHelper.INSTANCE.drawBorder(matrixStack, 0, 0, rect.getWidth(), rect.getHeight(), border);
		tooltip.draw(matrixStack);
		matrixStack.popPose();
	}

	@Override
	public @Nullable Component getMessage() {
		if (tooltip.getTooltip().isEmpty()) {
			return null;
		}
		return Component.literal(tooltip.getTooltip().getMessage());
	}

}
