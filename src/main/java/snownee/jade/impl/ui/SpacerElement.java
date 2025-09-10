package snownee.jade.impl.ui;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import snownee.jade.JadeInternals;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ResizeableElement;

public class SpacerElement extends ResizeableElement implements GuiEventListener {
	private LayoutElement wrapped;
	private int wrappedOffsetX;
	private int wrappedOffsetY;
	private @Nullable Predicate<? extends LayoutElement> onClick;

	public SpacerElement(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public SpacerElement wrapped(LayoutElement wrapped) {
		this.wrapped = wrapped;
		if (wrapped instanceof Element element) {
			if (element.cachedNarration() != null) {
				narration(element.cachedNarration());
			} else {
				narration("");
			}
			if (tag == null) {
				tag(element.getTag());
			}
		}
		return this;
	}

	@Override
	public @Nullable Component getNarration() {
		return null;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (wrapped instanceof Renderable renderable) {
			renderable.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void renderDebug(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, RenderDebugContext context) {
		super.renderDebug(graphics, mouseX, mouseY, partialTicks, context);
		if (wrapped instanceof Element element) {
			element.renderDebug(graphics, mouseX, mouseY, partialTicks, context);
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
		if (wrapped != null && (x != 0 || y != 0)) {
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

	@Override
	public ResizeableElement onClick(Predicate<Element> onClick) {
		this.onClick = onClick;
		return this;
	}

	@Override
	public boolean isMouseOver(double x, double y) {
		return wrapped != null && wrapped.getRectangle().containsPoint((int) x, (int) y);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
		if (mouseButtonEvent.button() == 0 && onClick != null && isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
			//noinspection unchecked
			return ((Predicate<LayoutElement>) onClick).test(wrapped);
		}
		return false;
	}

	@Override
	public void setFocused(boolean bl) {}

	@Override
	public boolean isFocused() {
		return false;
	}
}
