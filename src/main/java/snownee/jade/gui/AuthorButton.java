package snownee.jade.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import snownee.jade.util.SmoothChasingValue;

public class AuthorButton extends Button {
	private final Component hoveredTitle;
	private final OnPress onHover;
	private final SmoothChasingValue progress = new SmoothChasingValue();
	private boolean oldHovered;

	protected AuthorButton(
			int x,
			int y,
			int width,
			int height,
			Component title,
			Component hoveredTitle,
			OnPress onPress,
			OnPress onHover,
			CreateNarration createNarration) {
		super(x, y, width, height, title, onPress, createNarration);
		this.hoveredTitle = hoveredTitle;
		this.onHover = onHover;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float partialTicks) {
		boolean hovered = isHoveredOrFocused();
		if (!oldHovered && hovered) {
			progress.target(1);
		} else if (!hovered) {
			progress.target(0);
		} else if (progress.value > 0.5F) {
			progress.target(0);
			onHover.onPress(this);
		}
		progress.tick(partialTicks);
		progress.value = Math.min(0.6F, progress.value);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(getX() + width * 0.5F, getY(), 0);
		float scale = 1 + progress.value * 0.2F;
		guiGraphics.pose().scale(scale, scale, scale);
		Component credit = hovered ? hoveredTitle : getMessage();
		Font font = Minecraft.getInstance().font;
		guiGraphics.pose().translate(font.width(credit) * -0.5F, 0, 0);
		guiGraphics.drawString(font, credit, 0, 0, hovered ? 0xAAFFFFFF : 0x55FFFFFF);
		guiGraphics.pose().popPose();
		oldHovered = hovered;
	}
}
