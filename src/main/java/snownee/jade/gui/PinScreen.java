package snownee.jade.gui;

import java.util.Objects;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import snownee.jade.JadeClient;
import snownee.jade.api.ui.CopyBehavior;
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
			return OverlayRenderer.animation.mapMousePosition(event, $ -> root.mouseClicked($, doubleClick));
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

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		if (root != null && keyEvent.isCopy()) {
			Minecraft mc = Minecraft.getInstance();
			Window window = mc.getWindow();
			double mouseX = mc.mouseHandler.getScaledXPos(window);
			double mouseY = mc.mouseHandler.getScaledYPos(window);
			if (OverlayRenderer.animation.mapMousePosition(
					mouseX, mouseY, (x, y) -> {
						if (root.getChildAt(x, y).orElse(root) instanceof CopyBehavior behavior) {
							return behavior.copyToClipboard(mc.keyboardHandler);
						}
						return false;
					})) {
				Objects.requireNonNull(minecraft).getSoundManager().play(SimpleSoundInstance.forUI(
						SoundEvents.EXPERIENCE_ORB_PICKUP,
						1.0F));
			}
		}
		return super.keyPressed(keyEvent);
	}
}
