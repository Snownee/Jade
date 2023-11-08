package snownee.jade.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.overlay.DisplayHelper;

public class Tooltip implements ITooltip {

	public final List<Line> lines = new ArrayList<>();
	public boolean sneakyDetails;

	public static void drawDebugBorder(GuiGraphics guiGraphics, float x, float y, IElement element) {
		if (Jade.CONFIG.get().getGeneral().isDebug() && Screen.hasControlDown()) {
			Vec2 translate = element.getTranslation();
			Vec2 size = element.getCachedSize();
			DisplayHelper.INSTANCE.drawBorder(guiGraphics, x, y, x + size.x, y + size.y, 1, 0x88FF0000, true);
			if (!Vec2.ZERO.equals(translate)) {
				DisplayHelper.INSTANCE.drawBorder(guiGraphics, x + translate.x, y + translate.y, x + translate.x + size.x, y + translate.y + size.y, 1, 0x880000FF, true);
			}
		}
	}

	@Override
	public void clear() {
		lines.clear();
	}

	@Override
	public void append(int index, IElement element) {
		if (element.getTag() == null) {
			element.tag(ElementHelper.INSTANCE.currentUid());
		}
		if (isEmpty() || index == size()) {
			add(element);
		} else {
			lines.get(index).elements.add(element);
		}
	}

	@Override
	@Deprecated
	public IElementHelper getElementHelper() {
		return IElementHelper.get();
	}

	@Override
	public int size() {
		return lines.size();
	}

	@Override
	public void add(int index, IElement element) {
		if (element.getTag() == null) {
			element.tag(ElementHelper.INSTANCE.currentUid());
		}
		Line line = new Line();
		line.elements.add(element);
		lines.add(index, line);
	}

	@Override
	public List<IElement> get(ResourceLocation tag) {
		List<IElement> elements = Lists.newArrayList();
		for (Line line : lines) {
			line.sortedElements().stream().filter(e -> Objects.equal(tag, e.getTag())).forEach(elements::add);
		}
		return elements;
	}

	@Override
	public List<IElement> get(int index, Align align) {
		Line line = lines.get(index);
		return line.alignedElements(align);
	}

	@Override
	public void remove(ResourceLocation tag) {
		for (Iterator<Line> iterator = lines.iterator(); iterator.hasNext(); ) {
			Line line = iterator.next();
			if (line.elements.removeIf(e -> Objects.equal(tag, e.getTag()))) {
				line.markDirty();
				if (line.elements.isEmpty()) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public String getMessage() {
		List<String> msgs = Lists.newArrayList();
		for (Line line : lines) {
			/* off */
			msgs.add(Joiner.on(' ').join(
							line.sortedElements().stream()
									.filter(e -> !Identifiers.CORE_MOD_NAME.equals(e.getTag()))
									.map(IElement::getCachedMessage)
									.filter(java.util.Objects::nonNull)
									.toList()
					)
			);
			/* on */
		}
		return Joiner.on('\n').join(msgs);
	}

	@Override
	public void setLineMargin(int index, Direction2D side, int margin) {
		if (index < 0) {
			index += lines.size();
		}
		Line line = lines.get(index);
		switch (side) {
			case UP -> line.marginTop = margin;
			case DOWN -> line.marginBottom = margin;
			default -> throw new IllegalArgumentException("Only TOP and BOTTOM are allowed.");
		}
	}

	public static class Line {
		private final List<IElement> elements = Lists.newArrayList();
		private final int[] starts = new int[3-1];
		private final float[] widths = new float[3];
		public int marginTop = 0;
		public int marginBottom = 2;
		private Vec2 size;
		private boolean sorted;

		public void sort() {
			if (sorted)
				return;
			sorted = true;
			Arrays.fill(starts, 0);
			Arrays.fill(widths, 0);
			List<IElement> tempList = Lists.newArrayListWithExpectedSize(elements.size());
			float width = 0;
			float height = 0;
			for (IElement element : elements) {
				int index = element.getAlignment().ordinal();
				int start = index == 2 ? tempList.size() : starts[index];
				tempList.add(start, element);
				for (int i = index; i < starts.length; i++) {
					starts[i]++;
				}
				Vec2 elementSize = element.getCachedSize();
				widths[index] += elementSize.x;
				width += elementSize.x;
				height = Math.max(height, elementSize.y);
			}
			elements.clear();
			elements.addAll(tempList);
			size = new Vec2(width, height);
		}

		public void markDirty() {
			sorted = false;
			size = null;
		}

		public List<IElement> sortedElements() {
			sort();
			return elements;
		}

		public List<IElement> alignedElements(Align align) {
			sort();
			int index = align.ordinal();
			int start = index == 0 ? 0 : starts[index - 1];
			int end = index == 2 ? elements.size() : starts[index];
			return elements.subList(start, end);
		}

		public Vec2 size() {
			sort();
			return size;
		}

		public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
			sort();
			for (Align align : Align.VALUES) {
				renderAligned(guiGraphics, x, y, maxX, maxY, align);
			}
		}

		private void renderAligned(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY, Align align) {
			List<IElement> alignedElements = alignedElements(align);
			float ox = switch (align) {
				case LEFT   -> x;
				case RIGHT  -> maxX - widths[1];
				case CENTER -> {
					float left = x + widths[0];
					float right = maxX - widths[1];
					yield left + (right - left - widths[2]) / 2;
				}
			};

			boolean extendable = align == Align.LEFT && alignedElements.size() == elements.size();
			IElement lastElement = alignedElements.isEmpty() ? null : alignedElements.get(alignedElements.size() - 1);
			for (IElement element: alignedElements) {
				Vec2 translate = element.getTranslation();
				Vec2 size = element.getCachedSize();
				drawDebugBorder(guiGraphics, ox, y, element);
				element.render(guiGraphics, ox + translate.x, y + translate.y, (extendable && element == lastElement ? maxX : (ox + size.x)) + translate.x, maxY + translate.y);
				ox += size.x;
			}
		}
	}

}
