package mcp.mobius.waila.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IBorderStyle;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElement.Align;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.BorderStyle;
import mcp.mobius.waila.impl.ui.ElementHelper;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;

public class Tooltip implements ITooltip {

	public static class Line {
		private final List<IElement> left = new ArrayList<>();
		private final List<IElement> right = new ArrayList<>();
		private Vector2f size;

		public List<IElement> getAlignedElements(Align align) {
			return align == Align.LEFT ? left : right;
		}

		public Vector2f getSize() {
			if (size == null) {
				float width = 0, height = 0;
				for (IElement element : left) {
					Vector2f elementSize = element.getCachedSize();
					width += elementSize.x;
					height = Math.max(height, elementSize.y);
				}
				for (IElement element : right) {
					Vector2f elementSize = element.getCachedSize();
					width += elementSize.x;
					height = Math.max(height, elementSize.y);
				}
				size = new Vector2f(width, height);
			}
			return size;
		}

		public void render(MatrixStack matrixStack, float x, float y, float maxWidth, float y2) {
			float ox = maxWidth, oy = y;
			for (int i = right.size() - 1; i >= 0; i--) {
				IElement element = right.get(i);
				Vector2f translate = element.getTranslation();
				Vector2f size = element.getCachedSize();
				ox -= size.x;
				drawBorder(matrixStack, ox, oy, element);
				element.render(matrixStack, ox + translate.x, oy + translate.y, x + maxWidth, y + y2);
			}
			maxWidth = ox;
			ox = x;
			for (IElement element : left) {
				Vector2f translate = element.getTranslation();
				Vector2f size = element.getCachedSize();
				drawBorder(matrixStack, ox, oy, element);
				element.render(matrixStack, ox + translate.x, oy + translate.y, maxWidth, y + y2);
				ox += size.x;
			}
		}
	}

	public final List<Line> lines = new ArrayList<>();

	@Override
	public void clear() {
		lines.clear();
	}

	@Override
	public void append(int index, IElement element) {
		if (isEmpty() || index == size()) {
			add(element);
		} else {
			Line lastLine = lines.get(index);
			lastLine.getAlignedElements(element.getAlignment()).add(element);
		}
	}

	@Override
	public IElementHelper getElementHelper() {
		return ElementHelper.INSTANCE;
	}

	@Override
	public int size() {
		return lines.size();
	}

	@Override
	public void add(int index, IElement element) {
		Line line = new Line();
		line.getAlignedElements(element.getAlignment()).add(element);
		lines.add(index, line);
	}

	@Override
	public List<IElement> get(ResourceLocation tag) {
		List<IElement> elements = Lists.newArrayList();
		for (Line line : lines) {
			line.left.stream().filter(e -> Objects.equal(tag, e.getTag())).forEach(elements::add);
			line.right.stream().filter(e -> Objects.equal(tag, e.getTag())).forEach(elements::add);
		}
		return elements;
	}

	@Override
	public void remove(ResourceLocation tag) {
		for (Iterator<Line> iterator = lines.iterator(); iterator.hasNext();) {
			Line line = iterator.next();
			line.left.removeIf(e -> Objects.equal(tag, e.getTag()));
			line.right.removeIf(e -> Objects.equal(tag, e.getTag()));
			if (line.left.isEmpty() && line.right.isEmpty()) {
				iterator.remove();
			}
		}
	}

	private static final IBorderStyle RED = new BorderStyle().color(0x88FF0000);
	private static final IBorderStyle BLUE = new BorderStyle().color(0x880000FF);

	public static void drawBorder(MatrixStack matrixStack, float x, float y, IElement element) {
		if (Waila.CONFIG.get().getGeneral().isDebug()) {
			Vector2f translate = element.getTranslation();
			Vector2f size = element.getCachedSize();
			DisplayHelper.INSTANCE.drawBorder(matrixStack, x, y, x + size.x, y + size.y, RED);
			if (Vector2f.ZERO.equals(translate)) {
				DisplayHelper.INSTANCE.drawBorder(matrixStack, x + translate.x, y + translate.y, x + translate.x + size.x, y + translate.y + size.y, BLUE);
			}
		}
	}

}
