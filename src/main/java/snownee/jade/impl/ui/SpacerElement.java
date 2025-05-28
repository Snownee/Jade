package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import snownee.jade.JadeInternals;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ResizeableElement;

public class SpacerElement extends ResizeableElement {
	private LayoutElement wrapped;
	private int wrappedOffsetX;
	private int wrappedOffsetY;

	public SpacerElement(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public SpacerElement wrapped(LayoutElement wrapped) {
		this.wrapped = wrapped;
		return this;
	}

	@Override
	public @Nullable Component getNarration() {
		if (wrapped instanceof Element element) {
			return element.getNarration();
		}
		return null;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (wrapped instanceof Renderable renderable) {
			renderable.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void renderDebug(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderDebug(graphics, mouseX, mouseY, partialTicks);
		if (wrapped instanceof Element element) {
			element.renderDebug(graphics, mouseX, mouseY, partialTicks);
		}
		if (wrapped != null) {
			JadeInternals.getDisplayHelper().drawBorder(graphics, getRectangle(), 1, 0x880000FF, true);
		}
	}

	@Override
	public void setFreeSpace(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public SpacerElement offset(int x, int y) {
		wrappedOffsetX = x;
		wrappedOffsetY = y;
		if (wrapped != null) {
			wrapped.setX(x + wrappedOffsetX);
			wrapped.setY(y + wrappedOffsetY);
		}
		return this;
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		if (wrapped != null) {
			wrapped.setX(x + wrappedOffsetX);
		}
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		if (wrapped != null) {
			wrapped.setY(y + wrappedOffsetY);
		}
	}
}
