package snownee.jade.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.impl.ui.ElementHelper;

public class Tooltip implements ITooltip {

	public final List<Line> lines = new ArrayList<>();
	public boolean sneakyDetails;

	public Stream<LayoutElement> layoutElements() {
		return lines.stream().flatMap(line -> line.sortedElements().stream());
	}

	@Override
	public void clear() {
		lines.clear();
	}

	@Override
	public void append(int index, LayoutElement element) {
		if (element instanceof Element taggable && taggable.getTag() == null) {
			taggable.tag(ElementHelper.INSTANCE.currentUid());
		}
		if (isEmpty() || index == size()) {
			add(element);
		} else {
			Line line = lines.get(index);
			line.elements.add(element);
			line.markDirty();
		}
	}

	@Override
	public int size() {
		return lines.size();
	}

	@Override
	public void add(int index, LayoutElement element) {
		if (element instanceof Element taggable && taggable.getTag() == null) {
			taggable.tag(ElementHelper.INSTANCE.currentUid());
		}
		Line line = new Line();
		line.elements.add(element);
		lines.add(index, line);
	}

	@Override
	public List<LayoutElement> get(ResourceLocation tag) {
		List<LayoutElement> elements = Lists.newArrayList();
		//TODO
//		for (Line line : lines) {
//			line.sortedElements().stream().filter(e -> Objects.equal(tag, e.getTag())).forEach(elements::add);
//		}
		return elements;
	}

	@Override
	public List<LayoutElement> get(int index, Align align) {
		Line line = lines.get(index);
		return line.alignedElements(align);
	}

	@Override
	public boolean remove(ResourceLocation tag) {
		return removeInternal(tag, true, null);
	}

	private boolean removeInternal(ResourceLocation tag, boolean removeFirstLineIfEmpty, @Nullable List<List<LayoutElement>> collector) {
		boolean removed = false;
//		List<LayoutElement> collected = collector == null ? null : Lists.newArrayList();
//		for (Iterator<Line> iterator = lines.iterator(); iterator.hasNext(); ) {
//			Line line = iterator.next();
//			if (line.elements.removeIf(e -> {
//				if (Objects.equal(tag, e.getTag())) {
//					if (collector != null) {
//						collected.add(e);
//					}
//					return true;
//				}
//				return false;
//			})) {
//				line.markDirty();
//				if (line.elements.isEmpty() && (removed || removeFirstLineIfEmpty)) {
//					iterator.remove();
//				}
//				removed = true;
//				if (collector != null && !collected.isEmpty()) {
//					collector.add(Lists.newArrayList(collected));
//					collected.clear();
//				}
//			}
//		}
		return removed;
	}

	@Override
	public boolean replace(ResourceLocation tag, Component component) {
		return replace(tag, $ -> List.of(List.of(IElementHelper.get().text(component))));
	}

	@Override
	public boolean replace(ResourceLocation tag, UnaryOperator<List<List<LayoutElement>>> operator) {
		return false;//TODO
//		int firstX = -1, firstY = -1;
//		for (int y = 0; y < lines.size(); y++) {
//			Line line = lines.get(y);
//			for (int x = 0; x < line.sortedElements().size(); x++) {
//				LayoutElement element = line.sortedElements().get(x);
//				if (Objects.equal(tag, element.getTag())) {
//					if (firstX == -1) {
//						firstX = x;
//						firstY = y;
//					}
//				}
//			}
//		}
//		if (firstX != -1) {
//			List<List<LayoutElement>> elements = Lists.newArrayList();
//			removeInternal(tag, false, elements);
//			elements = operator.apply(elements);
//			for (List<LayoutElement> elementList : elements) {
//				for (LayoutElement element : elementList) {
//					if (element.getTag() == null) {
//						element.tag(tag);
//					}
//				}
//			}
//			for (int i = 0; i < elements.size(); i++) {
//				List<LayoutElement> list = elements.get(i);
//				if (i == 0) {
//					Line line = lines.get(firstY);
//					line.sortedElements().addAll(firstX, list);
//					line.markDirty();
//				} else {
//					add(firstY + i, list);
//				}
//			}
//		}
//		return firstX != -1;
	}

	@Override
	public String getNarration() {
		List<String> msgs = Lists.newArrayList();
		for (Line line : lines) {
			/* off */
//			msgs.add(String.join(
//					" ", line.sortedElements().stream()
//							.filter(e -> !JadeIds.CORE_MOD_NAME.equals(e.getTag()))
//							.map(LayoutElement::getCachedMessage)
//							.filter(java.util.Objects::nonNull)
//							.toList()));//TODO
			/* on */
		}
		return String.join("\n", msgs);
	}

	@Override
	public String getNarration(ResourceLocation tag) {
		return "";//TODO
//		return String.join(
//				" ", get(tag).stream()
//						.map(LayoutElement::getCachedMessage)
//						.filter(java.util.Objects::nonNull)
//						.toList());
	}

	@Override
	public void setLineMargin(int index, ScreenDirection side, int margin) {
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
		private final List<LayoutElement> elements = Lists.newArrayList();
		private final int[] starts = new int[3 - 1];
		private final float[] widths = new float[3];
		public int marginTop = 0;
		public int marginBottom = 2;
		private Vec2 size;
		private boolean sorted;

		public void sort() {
//			if (sorted) {
//				return;
//			}
//			sorted = true;
//			Arrays.fill(starts, 0);
//			Arrays.fill(widths, 0);
//			List<LayoutElement> tempList = Lists.newArrayListWithExpectedSize(elements.size());
//			float width = 0;
//			float height = 0;
//			for (LayoutElement element : elements) {
//				int index = element.getAlignment().ordinal();
//				int start = index == 2 ? tempList.size() : starts[index];
//				tempList.add(start, element);
//				for (int i = index; i < starts.length; i++) {
//					starts[i]++;
//				}
//				Vec2 elementSize = element.getCachedSize();
//				widths[index] += elementSize.x;
//				width += elementSize.x;
//				height = Math.max(height, elementSize.y);
//			}
//			elements.clear();
//			elements.addAll(tempList);
//			size = new Vec2(width, height);
		}

		public void markDirty() {
			sorted = false;
			size = null;
		}

		public List<LayoutElement> sortedElements() {
			sort();
			return elements;
		}

		public List<LayoutElement> alignedElements(Align align) {
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
	}

}
