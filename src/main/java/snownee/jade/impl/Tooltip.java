package snownee.jade.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.ui.TextElement;
import snownee.jade.impl.ui.JadeUIInternal;

public class Tooltip implements ITooltip {
	private static ResourceLocation getTag(LayoutElement element) {
		if (element instanceof Element taggable) {
			return taggable.getTag();
		}
		return null;
	}

	public final List<Line> lines = new ArrayList<>();
	public boolean sneakyDetails;
	public @Nullable Element icon;

	@Override
	public void clear() {
		lines.clear();
	}

	@Override
	public int size() {
		return lines.size();
	}

	@Override
	public void append(int index, LayoutElement element) {
		if (element instanceof Element taggable && taggable.getTag() == null) {
			taggable.tag(JadeUIInternal.contextUid());
		}
		if (isEmpty() || index == size()) {
			add(element);
		} else {
			Line line = lines.get(index);
			line.elements.add(element);
		}
	}

	@Override
	public void add(int index, LayoutElement element) {
		lines.add(index, new Line());
		append(index, element);
	}

	@Override
	public List<LayoutElement> get(ResourceLocation tag) {
		List<LayoutElement> elements = Lists.newArrayList();
		for (Line line : lines) {
			line.elements().stream().filter(e -> Objects.equal(tag, getTag(e))).forEach(elements::add);
		}
		return elements;
	}

	@Override
	public boolean remove(ResourceLocation tag) {
		return removeInternal(tag, true, null);
	}

	private boolean removeInternal(ResourceLocation tag, boolean removeFirstLineIfEmpty, @Nullable List<List<LayoutElement>> collector) {
		boolean removed = false;
		List<LayoutElement> collected = collector == null ? null : Lists.newArrayList();
		for (Iterator<Line> iterator = lines.iterator(); iterator.hasNext(); ) {
			Line line = iterator.next();
			if (line.elements.removeIf(e -> {
				if (Objects.equal(tag, getTag(e))) {
					if (collector != null) {
						collected.add(e);
					}
					return true;
				}
				return false;
			})) {
				if (line.elements.isEmpty() && (removed || removeFirstLineIfEmpty)) {
					iterator.remove();
				}
				removed = true;
				if (collector != null && !collected.isEmpty()) {
					collector.add(Lists.newArrayList(collected));
					collected.clear();
				}
			}
		}
		return removed;
	}

	@Override
	public boolean replace(ResourceLocation tag, Component component) {
		return replace(tag, $ -> List.of(List.of(JadeUI.text(component))));
	}

	@Override
	public boolean replace(ResourceLocation tag, UnaryOperator<List<List<LayoutElement>>> operator) {
		int firstX = -1, firstY = -1;
		for (int y = 0; y < lines.size(); y++) {
			Line line = lines.get(y);
			for (int x = 0; x < line.elements().size(); x++) {
				LayoutElement element = line.elements().get(x);
				if (Objects.equal(tag, getTag(element))) {
					if (firstX == -1) {
						firstX = x;
						firstY = y;
					}
				}
			}
		}
		if (firstX != -1) {
			List<List<LayoutElement>> elements = Lists.newArrayList();
			removeInternal(tag, false, elements);
			elements = operator.apply(elements);
			for (List<LayoutElement> elementList : elements) {
				for (LayoutElement element : elementList) {
					if (element instanceof Element taggable && taggable.getTag() == null) {
						taggable.tag(tag);
					}
				}
			}
			for (int i = 0; i < elements.size(); i++) {
				List<LayoutElement> list = elements.get(i);
				if (i == 0) {
					Line line = lines.get(firstY);
					line.elements().addAll(firstX, list);
				} else {
					add(firstY + i, list);
				}
			}
		}
		return firstX != -1;
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

	@Override
	public void setLineSettings(int index, UnaryOperator<LayoutSettings> settings) {
		if (index < 0) {
			index += lines.size();
		}
		Line line = lines.get(index);
		line.settings = settings;
	}

	@Override
	public String getNarration() {
		StringBuilder sb = new StringBuilder();
		NarrationElementOutput output = new NarrationElementOutput() {
			@Override
			public void add(NarratedElementType narratedElementType, NarrationThunk<?> narrationThunk) {
				if (narratedElementType != NarratedElementType.TITLE) {
					return;
				}
				narrationThunk.getText(text -> sb.append(text).append(". "));
			}

			@Override
			public @NotNull NarrationElementOutput nest() {
				return this;
			}
		};
		for (Line line : lines) {
			boolean hasSupplier = false;
			for (LayoutElement element : line.elements()) {
				if (element instanceof NarrationSupplier supplier) {
					supplier.updateNarration(output);
					hasSupplier = true;
				}
			}
			if (hasSupplier && !sb.isEmpty()) {
				sb.append('\n');
			}
		}
		if (sb.isEmpty()) {
			return "";
		}
		if (sb.charAt(sb.length() - 1) == '\n') {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	@Override
	public String getString(ResourceLocation tag) {
		return get(tag).stream().filter($ -> $ instanceof TextElement).map($ -> ((TextElement) $).getString()).findFirst().orElse("");
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		String narration = getNarration();
		if (!narration.isEmpty()) {
			narrationElementOutput.add(NarratedElementType.TITLE, narration);
		}
	}

	@Override
	public @Nullable Element getIcon() {
		return icon;
	}

	public void setIcon(@Nullable Element icon) {
		this.icon = icon;
	}

	public static class Line {
		private final List<LayoutElement> elements = Lists.newArrayList();
		public int marginTop = 0;
		public int marginBottom = 2;
		public @Nullable UnaryOperator<LayoutSettings> settings;

		public List<LayoutElement> elements() {
			return elements;
		}
	}

}
