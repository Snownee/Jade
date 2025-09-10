package snownee.jade.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import snownee.jade.JadeClient;
import snownee.jade.impl.ui.BoxElementImpl;
import snownee.jade.overlay.OverlayRenderer;

public class PinScreen extends Screen {
	public PinScreen() {
		super(Component.translatable("gui.jade.pin"));
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		// No background rendering needed
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(
					event.x(),
					event.y(),
					(x0, y0) -> root.mouseClicked(event, doubleClick));
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(event, root::mouseReleased);
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double deltaX, double deltaY) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(x, y, (x0, y0) -> root.mouseScrolled(x0, y0, deltaX, deltaY));
		}
		return super.mouseScrolled(x, y, deltaX, deltaY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(event, $ -> root.mouseDragged($, deltaX, deltaY));
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public void mouseMoved(double x, double y) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			OverlayRenderer.animation.mapMousePosition(
					x, y, (x0, y0) -> {
						root.mouseMoved(x0, y0);
						return null;
					});
		} else {
			super.mouseMoved(x, y);
		}
	}
}
