package snownee.jade.gui;

import java.util.function.Consumer;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

public class LayoutWithPadding implements Layout {
	private final LayoutElement wrapped;
	public int paddingLeft;
	public int paddingTop;
	public int paddingRight;
	public int paddingBottom;

	public LayoutWithPadding(LayoutElement wrapped) {
		this.wrapped = wrapped;
	}

	public LayoutWithPadding(LayoutElement wrapped, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
		this.wrapped = wrapped;
		this.paddingLeft = paddingLeft;
		this.paddingTop = paddingTop;
		this.paddingRight = paddingRight;
		this.paddingBottom = paddingBottom;
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		if (wrapped instanceof Layout layout) {
			layout.visitChildren(consumer);
		} else {
			consumer.accept(wrapped);
		}
	}

	@Override
	public void arrangeElements() {
		if (wrapped instanceof Layout layout) {
			layout.arrangeElements();
			setPosition(layout.getX(), layout.getY());
		}
	}

	@Override
	public int getWidth() {
		return wrapped.getWidth() + paddingLeft + paddingRight;
	}

	@Override
	public int getHeight() {
		return wrapped.getHeight() + paddingTop + paddingBottom;
	}

	@Override
	public void setX(int x) {
		wrapped.setX(x + paddingLeft);
	}

	@Override
	public void setY(int y) {
		wrapped.setY(y + paddingTop);
	}

	@Override
	public void setPosition(int x, int y) {
		wrapped.setPosition(x + paddingLeft, y + paddingTop);
	}

	@Override
	public int getX() {
		return wrapped.getX() - paddingLeft;
	}

	@Override
	public int getY() {
		return wrapped.getY() - paddingTop;
	}
}
