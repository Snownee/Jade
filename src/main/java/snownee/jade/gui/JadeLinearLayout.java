package snownee.jade.gui;

import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.util.Util;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.Orientation;

public class JadeLinearLayout extends AbstractLayout implements ResizeableLayout {
	private Orientation orientation;
	private Align alignItems = Align.START;
	private final List<ChildContainer> children = Lists.newArrayList();
	private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();
	private int defaultHeadMargin;
	private int defaultTailMargin;
	private int minWidth;
	private int minHeight;
	private int flexGrow;
	private boolean arranged;

	public JadeLinearLayout(Orientation orientation) {
		super(0, 0, 0, 0);
		this.orientation = orientation;
	}

	public JadeLinearLayout orientation(Orientation orientation) {
		this.orientation = orientation;
		arranged = false;
		return this;
	}

	public JadeLinearLayout alignItems(Align align) {
		this.alignItems = align;
		arranged = false;
		return this;
	}

	public JadeLinearLayout spacing(int i) {
		defaultHeadMargin = defaultTailMargin = i;
		arranged = false;
		return this;
	}

	public JadeLinearLayout setMinDimensions(int minWidth, int minHeight) {
		return setMinWidth(minWidth).setMinHeight(minHeight);
	}

	public JadeLinearLayout setMinHeight(int minHeight) {
		this.minHeight = minHeight;
		arranged = false;
		return this;
	}

	public JadeLinearLayout setMinWidth(int minWidth) {
		this.minWidth = minWidth;
		arranged = false;
		return this;
	}

	public LayoutSettings newChildLayoutSettings() {
		return defaultChildLayoutSettings.copy();
	}

	public LayoutSettings defaultChildLayoutSetting() {
		return defaultChildLayoutSettings;
	}

	public <T extends LayoutElement> T addChild(T element) {
		return addChild(element, newChildLayoutSettings(element));
	}

	public <T extends LayoutElement> T addChild(T element, Consumer<LayoutSettings> consumer) {
		return addChild(element, Util.make(newChildLayoutSettings(element), consumer));
	}

	public <T extends LayoutElement> T addChild(T element, LayoutSettings layoutSettings) {
		return addChild(element, layoutSettings, null);
	}

	public <T extends LayoutElement> T addChild(T element, LayoutSettings layoutSettings, @Nullable Consumer<ChildContainer> consumer) {
		ChildContainer container = new ChildContainer(element, layoutSettings);
		container.headMargin = defaultHeadMargin;
		container.tailMargin = defaultTailMargin;
		if (consumer != null) {
			consumer.accept(container);
		}
		children.add(container);
		arranged = false;
		return element;
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		this.children.forEach(childContainer -> consumer.accept(childContainer.child));
	}

	@Override
	public void arrangeElements() {
		if (arranged) {
			return;
		}
		int size = children.size();
		if (size == 0) {
			width = height = 0;
			arranged = true;
			return;
		}
		super.arrangeElements();
		int axis = 0;
		int crossAxis = 0;
		int sumGrow = 0;
		int[] margins = null;
		if (size == 1) {
			ChildContainer child = children.getFirst();
			axis = orientation.getAxisLength(child);
			crossAxis = orientation.getCrossAxisLength(child);
			sumGrow = child.flexGrow;
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
				sumGrow += child.flexGrow;

				lastChild = child;
			}
			arranged = true;
		}

		int minAxis = orientation == Orientation.HORIZONTAL ? minWidth : minHeight;
		int extraAxisSpace = Math.max(0, minAxis - axis);
		axis = Math.max(axis, minAxis);
		int minCrossAxis = orientation == Orientation.HORIZONTAL ? minHeight : minWidth;
		crossAxis = Math.max(crossAxis, minCrossAxis);

		resolveFlexGrow(extraAxisSpace, crossAxis, sumGrow);

		int axisPos = orientation.getAxisPosition(this);
		int crossAxisPos = orientation.getCrossAxisPosition(this);
		for (int i = 0; i < size; i++) {
			ChildContainer child = children.get(i);
			int childAxisLength = orientation.getAxisLength(child);

			if (i != 0) {
				axisPos += margins[i - 1];
			}

			Align align = MoreObjects.firstNonNull(child.alignSelf, alignItems);
			align.align(orientation, child, axisPos, crossAxisPos, crossAxis);

			axisPos += childAxisLength;
		}

		width = orientation == Orientation.HORIZONTAL ? axis : crossAxis;
		height = orientation == Orientation.HORIZONTAL ? crossAxis : axis;
	}

	private void resolveFlexGrow(int extraAxisSpace, int crossAxis, int sumGrow) {
		if (sumGrow == 0 || extraAxisSpace <= 0) {
			return;
		}

		List<ChildContainer> children = this.children.stream()
				.filter(it -> it.flexGrow > 0)
				.toList();
		int size = children.size();
		if (size == 1) {
			ChildContainer child = children.getFirst();
			orientation.setFreeSpace(child, orientation.getAxisLength(child) + extraAxisSpace, crossAxis);
			return;
		}

		int reachLimitAmount = 0;
		BitSet reachLimitFlags = new BitSet(size);
		boolean changed = true;
		outer:
		while (changed && reachLimitAmount < size && extraAxisSpace > 0) {
			changed = false;
			int virtualSumGrow = sumGrow;
			for (int i = 0; i < size; i++) {
				ChildContainer child = children.get(i);
				if (reachLimitFlags.get(i)) {
					continue;
				}
				int childAxisLength = orientation.getAxisLength(child);
				int grow = child.flexGrow;
				int extraChildAxis = extraAxisSpace * grow / virtualSumGrow;
				if (extraChildAxis <= 0) {
					continue;
				}
				int newAxisLength = childAxisLength + extraChildAxis;
				orientation.setFreeSpace(child, newAxisLength, crossAxis);
				int childAxisLengthNow = orientation.getAxisLength(child);
				changed |= childAxisLength != childAxisLengthNow;
				if (newAxisLength > childAxisLengthNow) {
					reachLimitFlags.set(i);
					reachLimitAmount++;
					sumGrow -= grow;
					if (sumGrow <= 0) {
						break outer; // no more children to grow
					}
				}
				extraAxisSpace -= extraChildAxis;
				virtualSumGrow -= grow;
				if (virtualSumGrow <= 0) {
					break;
				}
			}
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
		return new JadeLinearLayout(Orientation.VERTICAL);
	}

	public static JadeLinearLayout horizontal() {
		return new JadeLinearLayout(Orientation.HORIZONTAL);
	}

	@Override
	public void setFreeSpace(int width, int height) {
		if (this.width >= width && this.height >= height) {
			return; // no need to resize
		}
		int oldMinWidth = minWidth;
		int oldMinHeight = minHeight;
		minWidth = Math.max(minWidth, width);
		minHeight = Math.max(minHeight, height);
		arranged = false;
		arrangeElements();
		minWidth = oldMinWidth;
		minHeight = oldMinHeight;
	}

	@Override
	public void setFlexGrow(int flexGrow) {
		Preconditions.checkArgument(flexGrow >= 0, "flexGrow must be non-negative");
		this.flexGrow = flexGrow;
		arranged = false;
	}

	@Override
	public int getFlexGrow() {
		return flexGrow;
	}

	public LayoutSettings newChildLayoutSettings(LayoutElement layoutElement) {
		LayoutSettings settings = newChildLayoutSettings();
		if (layoutElement instanceof Element element && element.getSettings() != null) {
			settings = element.getSettings().apply(settings);
		}
		return settings;
	}

	@Override
	public int getHeight() {
		if (!arranged) {
			arrangeElements();
		}
		return super.getHeight();
	}

	@Override
	public int getWidth() {
		if (!arranged) {
			arrangeElements();
		}
		return super.getWidth();
	}

	public static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
		public int headMargin;
		public int tailMargin;
		public int flexGrow;
		public @Nullable Align alignSelf;

		protected ChildContainer(LayoutElement element, LayoutSettings settings) {
			super(element, settings);
		}
	}

	public enum Align {
		START, CENTER, END, STRETCH;

		private void align(Orientation orientation, ChildContainer child, int axisPos, int crossAxisPos, int crossAxisFreeSpace) {
			int axisLength = orientation.getAxisLength(child);
			int crossAxisLength = orientation.getCrossAxisLength(child);
			switch (this) {
				case START -> {
					// do nothing
				}
				case CENTER -> crossAxisPos += (crossAxisFreeSpace - crossAxisLength) / 2;
				case END -> crossAxisPos += crossAxisFreeSpace - crossAxisLength;
				case STRETCH -> orientation.setFreeSpace(child, axisLength, crossAxisFreeSpace); // stretch to fill the cross axis
			}
			orientation.setPosition(child, axisPos, crossAxisPos);
		}
	}
}
