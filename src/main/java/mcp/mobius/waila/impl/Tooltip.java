package mcp.mobius.waila.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IBorderStyle;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElement.Align;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.BorderStyle;
import mcp.mobius.waila.impl.ui.ElementHelper;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class Tooltip implements ITooltip {

	public static class Line {
		private final List<IElement> left = new ArrayList<>();
		private final List<IElement> right = new ArrayList<>();
		private Vec2 size;

		public List<IElement> getAlignedElements(Align align) {
			return align == Align.LEFT ? left : right;
		}

		public Vec2 getSize() {
			if (size == null) {
				float width = 0, height = 0;
				for (IElement element : left) {
					Vec2 elementSize = element.getCachedSize();
					width += elementSize.x;
					height = Math.max(height, elementSize.y);
				}
				for (IElement element : right) {
					Vec2 elementSize = element.getCachedSize();
					width += elementSize.x;
					height = Math.max(height, elementSize.y);
				}
				size = new Vec2(width, height);
			}
			return size;
		}

		public void render(PoseStack matrixStack, float x, float y, float maxWidth, float y2) {
			float ox = maxWidth, oy = y;
			for (int i = right.size() - 1; i >= 0; i--) {
				IElement element = right.get(i);
				Vec2 translate = element.getTranslation();
				Vec2 size = element.getCachedSize();
				ox -= size.x;
				drawBorder(matrixStack, ox, oy, element);
				element.render(matrixStack, ox + translate.x, oy + translate.y, x + maxWidth, y + y2);
			}
			maxWidth = ox;
			ox = x;
			for (IElement element : left) {
				Vec2 translate = element.getTranslation();
				Vec2 size = element.getCachedSize();
				drawBorder(matrixStack, ox, oy, element);
				element.render(matrixStack, ox + translate.x, oy + translate.y, maxWidth, y + y2);
				ox += size.x;
			}
		}
	}

	public final List<Line> lines = new ArrayList<>();
	public boolean sneakyDetails;

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
	public List<IElement> get(int index, Align align) {
		Line line = lines.get(index);
		return line.getAlignedElements(align);
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

	public static void drawBorder(PoseStack matrixStack, float x, float y, IElement element) {
		if (Waila.CONFIG.get().getGeneral().isDebug()) {
			Vec2 translate = element.getTranslation();
			Vec2 size = element.getCachedSize();
			DisplayHelper.INSTANCE.drawBorder(matrixStack, x, y, x + size.x, y + size.y, RED);
			if (!Vec2.ZERO.equals(translate)) {
				DisplayHelper.INSTANCE.drawBorder(matrixStack, x + translate.x, y + translate.y, x + translate.x + size.x, y + translate.y + size.y, BLUE);
			}
		}
	}

}
