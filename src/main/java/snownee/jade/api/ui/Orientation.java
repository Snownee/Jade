package snownee.jade.api.ui;

import net.minecraft.client.gui.layouts.LayoutElement;
import snownee.jade.gui.JadeLinearLayout;
import snownee.jade.gui.ResizeableLayout;

public enum Orientation {
	HORIZONTAL, VERTICAL;

	public int getAxisLength(JadeLinearLayout.ChildContainer child) {
		return this == HORIZONTAL ? child.getWidth() : child.getHeight();
	}

	public int getCrossAxisLength(JadeLinearLayout.ChildContainer child) {
		return this == HORIZONTAL ? child.getHeight() : child.getWidth();
	}

	public int getAxisPosition(LayoutElement element) {
		return this == HORIZONTAL ? element.getX() : element.getY();
	}

	public int getCrossAxisPosition(LayoutElement element) {
		return this == HORIZONTAL ? element.getY() : element.getX();
	}

	public void setPosition(JadeLinearLayout.ChildContainer child, int axis, int crossAxis) {
		if (this == HORIZONTAL) {
			child.setX(axis, child.getWidth());
			child.setY(crossAxis, child.getHeight());
		} else {
			child.setX(crossAxis, child.getWidth());
			child.setY(axis, child.getHeight());
		}
	}

	public void setFreeSpace(JadeLinearLayout.ChildContainer child, int axis, int crossAxis) {
		if (child.child instanceof ResizeableLayout resizeableLayout) {
			if (this == Orientation.HORIZONTAL) {
				resizeableLayout.setFreeSpace(axis, crossAxis);
			} else {
				resizeableLayout.setFreeSpace(crossAxis, axis);
			}
		} else {
			throw new IllegalStateException("Child " + child.child + " is not a ResizeableLayout");
		}
	}
}
