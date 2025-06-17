package snownee.jade.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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
	public boolean mouseClicked(double x, double y, int button) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(x, y, (x0, y0) -> root.mouseClicked(x0, y0, button));
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean mouseReleased(double x, double y, int button) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(x, y, (x0, y0) -> root.mouseReleased(x0, y0, button));
		}
		return super.mouseReleased(x, y, button);
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
	public boolean mouseDragged(double x, double y, int button, double deltaX, double deltaY) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null) {
			return OverlayRenderer.animation.mapMousePosition(x, y, (x0, y0) -> root.mouseDragged(x0, y0, button, deltaX, deltaY));
		}
		return super.mouseDragged(x, y, button, deltaX, deltaY);
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
