package snownee.jade.api.ui;

import net.minecraft.client.gui.layouts.LayoutElement;
import snownee.jade.gui.JadeLinearLayout;
import snownee.jade.gui.ResizeableLayout;

/**
 * Primary layout orientation for Jade UI containers.
 */
public enum Orientation {
	HORIZONTAL, VERTICAL;

	public int getAxisLength(JadeLinearLayout.ChildContainer child) {
		return this == HORIZONTAL ? child.getWidth() : child.getHeight();
	}

	public int getCrossAxisLength(JadeLinearLayout.ChildContainer child) {
		return this == HORIZONTAL ? child.getHeight() : child.getWidth();
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

	public int getAxisPosition(LayoutElement element) {
		return this == HORIZONTAL ? element.getX() : element.getY();
	}

	public int getCrossAxisPosition(LayoutElement element) {
		return this == HORIZONTAL ? element.getY() : element.getX();
	}

	public int getAxisLength(LayoutElement element) {
		return this == HORIZONTAL ? element.getWidth() : element.getHeight();
	}

	public int getCrossAxisLength(LayoutElement element) {
		return this == HORIZONTAL ? element.getHeight() : element.getWidth();
	}

	public void setPosition(LayoutElement element, int axis, int crossAxis) {
		if (this == HORIZONTAL) {
			element.setPosition(axis, crossAxis);
		} else {
			element.setPosition(crossAxis, axis);
		}
	}

	public void setFreeSpace(LayoutElement element, int axis, int crossAxis) {
		if (element instanceof ResizeableLayout resizeableLayout) {
			if (this == HORIZONTAL) {
				resizeableLayout.setFreeSpace(axis, crossAxis);
			} else {
				resizeableLayout.setFreeSpace(crossAxis, axis);
			}
		} else {
			throw new IllegalStateException("Element " + element + " is not a ResizeableLayout");
		}
	}

	public float getAxisLength(Rect2f rect) {
		return this == HORIZONTAL ? rect.getWidth() : rect.getHeight();
	}

	public float getCrossAxisLength(Rect2f rect) {
		return this == HORIZONTAL ? rect.getHeight() : rect.getWidth();
	}

	public float getAxisPosition(Rect2f rect) {
		return this == HORIZONTAL ? rect.getX() : rect.getY();
	}

	public float getCrossAxisPosition(Rect2f rect) {
		return this == HORIZONTAL ? rect.getY() : rect.getX();
	}

	public void setPosition(Rect2f rect, float axis, float crossAxis) {
		if (this == HORIZONTAL) {
			rect.setPosition(axis, crossAxis);
		} else {
			rect.setPosition(crossAxis, axis);
		}
	}

	public void setSize(Rect2f rect, float axis, float crossAxis) {
		if (this == HORIZONTAL) {
			rect.setWidth(axis);
			rect.setHeight(crossAxis);
		} else {
			rect.setWidth(crossAxis);
			rect.setHeight(axis);
		}
	}
}
