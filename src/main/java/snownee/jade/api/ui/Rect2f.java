package snownee.jade.api.ui;

import com.google.common.base.Objects;

import net.minecraft.client.gui.layouts.LayoutElement;

public class Rect2f {
	private float xPos;
	private float yPos;
	private float width;
	private float height;

	public Rect2f() {}

	public Rect2f(float x, float y, float width, float height) {
		this.xPos = x;
		this.yPos = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Rect2f rect2f = (Rect2f) o;
		return Float.compare(xPos, rect2f.xPos) == 0 && Float.compare(yPos, rect2f.yPos) == 0 &&
				Float.compare(width, rect2f.width) == 0 && Float.compare(height, rect2f.height) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(xPos, yPos, width, height);
	}

	public static Rect2f of(LayoutElement element) {
		return new Rect2f(
				element.getX(),
				element.getY(),
				element.getWidth(),
				element.getHeight());
	}

	public boolean intersects(Rect2f otherRect) {
		return this.xPos < otherRect.getX() + otherRect.getWidth() && this.xPos + this.width > otherRect.getX() &&
				this.yPos < otherRect.getY() + otherRect.getHeight() && this.yPos + this.height > otherRect.getY();
	}

	public Rect2f intersect(Rect2f otherRect) {
		float x1 = this.xPos;
		float y1 = this.yPos;
		float x2 = this.xPos + this.width;
		float y2 = this.yPos + this.height;

		float otherX1 = otherRect.getX();
		float otherY1 = otherRect.getY();
		float otherX2 = otherX1 + otherRect.getWidth();
		float otherY2 = otherY1 + otherRect.getHeight();

		this.xPos = Math.max(x1, otherX1);
		this.yPos = Math.max(y1, otherY1);
		this.width = Math.max(0.0F, Math.min(x2, otherX2) - this.xPos);
		this.height = Math.max(0.0F, Math.min(y2, otherY2) - this.yPos);
		return this;
	}

	public float getX() {
		return this.xPos;
	}

	public float getY() {
		return this.yPos;
	}

	public void setX(float x) {
		this.xPos = x;
	}

	public void setY(float y) {
		this.yPos = y;
	}

	public float getWidth() {
		return this.width;
	}

	public float getHeight() {
		return this.height;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public void setPosition(float x, float y) {
		this.xPos = x;
		this.yPos = y;
	}

	public boolean contains(float x, float y) {
		return x >= this.xPos && x <= this.xPos + this.width && y >= this.yPos && y <= this.yPos + this.height;
	}

	public Rect2f copy() {
		return new Rect2f(xPos, yPos, width, height);
	}

	public void copy(Rect2f src) {
		this.xPos = src.xPos;
		this.yPos = src.yPos;
		this.width = src.width;
		this.height = src.height;
	}

	public float getRight() {
		return xPos + width;
	}

	public float getBottom() {
		return yPos + height;
	}

	public Rect2f inflate(float amount) {
		this.xPos -= amount;
		this.yPos -= amount;
		this.width += amount * 2;
		this.height += amount * 2;
		return this;
	}
}
