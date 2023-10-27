package snownee.jade.api.ui;

import java.util.List;

public enum Direction2D {
	UP, RIGHT, DOWN, LEFT;

	public static final List<Direction2D> VALUES = List.of(values());

	public static Direction2D fromIndex(int index) {
		return VALUES.get(index);
	}

	public boolean isHorizontal() {
		return this == LEFT || this == RIGHT;
	}

	public boolean isVertical() {
		return this == UP || this == DOWN;
	}
}
