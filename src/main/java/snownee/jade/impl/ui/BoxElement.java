package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IBoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.ITooltipRenderer;
import snownee.jade.impl.Tooltip;
import snownee.jade.overlay.TooltipRenderer;

public class BoxElement extends Element implements IBoxElement {
	private final TooltipRenderer tooltip;
	private final IBoxStyle box;

	public BoxElement(Tooltip tooltip, IBoxStyle box) {
		this.tooltip = new TooltipRenderer(tooltip, false);
		this.box = box;
	}

	@Override
	public Vec2 getSize() {
		if (tooltip.getTooltip().isEmpty()) {
			return Vec2.ZERO;
		}
		Vec2 size = tooltip.getSize();
		return new Vec2(size.x, size.y + 1);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		if (tooltip.getTooltip().isEmpty()) {
			return;
		}
		// Rect2i rect = tooltip.getPosition();
		RenderSystem.enableBlend();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(x, y, 0);
		box.render(guiGraphics, 0, 0, maxX - x, maxY - y - 2);
		tooltip.setSize(new Vec2(maxX - x, tooltip.getSize().y));
		tooltip.draw(guiGraphics);
		guiGraphics.pose().popPose();
	}

	@Override
	public IElement tag(ResourceLocation tag) {
		box.tag(tag);
		return super.tag(tag);
	}

	@Override
	public @Nullable String getMessage() {
		if (tooltip.getTooltip().isEmpty()) {
			return null;
		}
		return tooltip.getTooltip().getMessage();
	}

	@Override
	public ITooltipRenderer getTooltipRenderer() {
		return tooltip;
	}

}
