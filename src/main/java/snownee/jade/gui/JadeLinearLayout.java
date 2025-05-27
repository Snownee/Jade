package snownee.jade.gui;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.Util;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

public class JadeLinearLayout extends AbstractLayout implements ResizeableLayout {
	private final Orientation orientation;
	private final List<ChildContainer> children = Lists.newArrayList();
	private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();
	private int defaultHeadMargin;
	private int defaultTailMargin;
	private int minWidth;
	private int minHeight;

	public JadeLinearLayout(Orientation orientation) {
		super(0, 0, 0, 0);
		this.orientation = orientation;
	}

	public JadeLinearLayout spacing(int i) {
		defaultHeadMargin = defaultTailMargin = i;
		return this;
	}

	public JadeLinearLayout setMinDimensions(int minWidth, int minHeight) {
		return setMinWidth(minWidth).setMinHeight(minHeight);
	}

	public JadeLinearLayout setMinHeight(int minHeight) {
		this.minHeight = minHeight;
		return this;
	}

	public JadeLinearLayout setMinWidth(int minWidth) {
		this.minWidth = minWidth;
		return this;
	}

	public LayoutSettings newChildLayoutSettings() {
		return defaultChildLayoutSettings.copy();
	}

	public LayoutSettings defaultChildLayoutSetting() {
		return defaultChildLayoutSettings;
	}

	public <T extends LayoutElement> T addChild(T element) {
		return addChild(element, newChildLayoutSettings());
	}

	public <T extends LayoutElement> T addChild(T element, Consumer<LayoutSettings> consumer) {
		return addChild(element, Util.make(newChildLayoutSettings(), consumer));
	}

	public <T extends LayoutElement> T addChild(T element, LayoutSettings layoutSettings) {
		ChildContainer container = new ChildContainer(element, layoutSettings);
		container.headMargin = defaultHeadMargin;
		container.tailMargin = defaultTailMargin;
		children.add(container);
		return element;
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		this.children.forEach(childContainer -> consumer.accept(childContainer.child));
	}

	@Override
	public void arrangeElements() {
		int size = children.size();
		if (size == 0) {
			return;
		}
		super.arrangeElements();
		int axis = 0;
		int crossAxis = 0;
		int[] margins = null;
		if (size == 1) {
			axis = orientation.getAxisLength(children.getFirst());
			crossAxis = orientation.getCrossAxisLength(children.getFirst());
		} else {
			margins = new int[size - 1];
			ChildContainer lastChild = null;
			for (int i = 0; i < size; i++) {
				ChildContainer child = children.get(i);
				if (i != 0) {
					int margin = calculateMargin(lastChild.tailMargin, child.headMargin);
					margins[i - 1] = margin;
					axis += margin;
				}

				axis += orientation.getAxisLength(child);
				crossAxis = Math.max(crossAxis, orientation.getCrossAxisLength(child));

				lastChild = child;
			}
		}

		int minAxis = orientation == Orientation.HORIZONTAL ? minWidth : minHeight;
//		int freeAxisSpace = Math.max(0, getWidth() - axis);
		axis = Math.max(axis, minAxis);
		int minCrossAxis = orientation == Orientation.HORIZONTAL ? minHeight : minWidth;
		crossAxis = Math.max(crossAxis, minCrossAxis);

		int axisPos = orientation.getAxisPosition(this);
		int crossAxisPos = orientation.getCrossAxisPosition(this);
		for (int i = 0; i < size; i++) {
			ChildContainer child = children.get(i);
			int childAxisLength = orientation.getAxisLength(child);
//			int childCrossAxisLength = orientation.getCrossAxisLength(child);

			if (i != 0) {
				axisPos += margins[i];
			}

			orientation.setPosition(child, axisPos, crossAxisPos);

			axisPos += childAxisLength;
		}
	}

	private static int calculateMargin(int margin1, int margin2) {
		if (margin1 >= 0 && margin2 >= 0) {
			return Math.max(margin1, margin2);
		} else if (margin1 < 0 && margin2 < 0) {
			return Math.min(margin1, margin2);
		} else {
			return margin1 + margin2;
		}
	}

	public static JadeLinearLayout vertical() {
		return new JadeLinearLayout(JadeLinearLayout.Orientation.VERTICAL);
	}

	public static JadeLinearLayout horizontal() {
		return new JadeLinearLayout(JadeLinearLayout.Orientation.HORIZONTAL);
	}

	static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
		int headMargin;
		int tailMargin;

		protected ChildContainer(LayoutElement element, LayoutSettings settings) {
			super(element, settings);
		}
	}

	public enum Orientation {
		HORIZONTAL, VERTICAL;

		private int getAxisLength(ChildContainer child) {
			return this == HORIZONTAL ? child.getHeight() : child.getWidth();
		}

		private int getCrossAxisLength(ChildContainer child) {
			return this == HORIZONTAL ? child.getWidth() : child.getHeight();
		}

		private int getAxisPosition(LayoutElement element) {
			return this == HORIZONTAL ? element.getX() : element.getY();
		}

		private int getCrossAxisPosition(LayoutElement element) {
			return this == HORIZONTAL ? element.getY() : element.getX();
		}

		private void setPosition(ChildContainer child, int axis, int crossAxis) {
			if (this == HORIZONTAL) {
				child.setX(axis, child.getWidth());
				child.setY(crossAxis, child.getHeight());
			} else {
				child.setX(crossAxis, child.getWidth());
				child.setY(axis, child.getHeight());
			}
		}
	}
}
