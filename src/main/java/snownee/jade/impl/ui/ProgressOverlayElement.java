package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import snownee.jade.api.ui.Rect2f;
import snownee.jade.api.ui.ResizeableElement;

public abstract class ProgressOverlayElement extends ResizeableElement {
	protected @Nullable Rect2f floatingRect;

	public void setFloatingRect(float x, float y, float width, float height) {
		if (floatingRect == null) {
			floatingRect = new Rect2f(x, y, width, height);
		} else {
			floatingRect.setPosition(x, y);
			floatingRect.setWidth(width);
			floatingRect.setHeight(height);
		}
	}

	public void setFloatingRect(@Nullable Rect2f floatingRect) {
		this.floatingRect = floatingRect;
	}

	public @Nullable Rect2f getFloatingRect() {
		return floatingRect;
	}
}
