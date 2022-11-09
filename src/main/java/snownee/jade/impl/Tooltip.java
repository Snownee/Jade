package snownee.jade.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.IBorderStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.BorderStyle;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.overlay.DisplayHelper;

public class Tooltip implements ITooltip {

	public static class Line {
		private final List<IElement> left = new ArrayList<>();
		private final List<IElement> right = new ArrayList<>(0);
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
				element.render(matrixStack, ox + translate.x, oy + translate.y, x + size.x + translate.x, y + y2 + translate.y);
			}
			maxWidth = ox;
			ox = x;
			for (int i = 0; i < left.size(); i++) {
				IElement element = left.get(i);
				Vec2 translate = element.getTranslation();
				Vec2 size = element.getCachedSize();
				drawBorder(matrixStack, ox, oy, element);
				element.render(matrixStack, ox + translate.x, oy + translate.y, ((i == left.size() - 1) ? maxWidth : (ox + size.x)) + translate.x, y + y2 + translate.y);
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
		if (element.getTag() == null) {
			element.tag(ElementHelper.INSTANCE.currentUid());
		}
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
		if (element.getTag() == null) {
			element.tag(ElementHelper.INSTANCE.currentUid());
		}
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
		if (Jade.CONFIG.get().getGeneral().isDebug()) {
			Vec2 translate = element.getTranslation();
			Vec2 size = element.getCachedSize();
			DisplayHelper.INSTANCE.drawBorder(matrixStack, x, y, x + size.x, y + size.y, RED);
			if (!Vec2.ZERO.equals(translate)) {
				DisplayHelper.INSTANCE.drawBorder(matrixStack, x + translate.x, y + translate.y, x + translate.x + size.x, y + translate.y + size.y, BLUE);
			}
		}
	}

	@Override
	public String getMessage() {
		List<String> msgs = Lists.newArrayList();
		for (Line line : lines) {
			/* off */
			msgs.add(Joiner.on(' ').join(
							Stream.concat(line.left.stream(), line.right.stream())
									.filter(e -> !Identifiers.CORE_MOD_NAME.equals(e.getTag()))
									.map(IElement::getCachedMessage)
									.filter(java.util.Objects::nonNull)
									.map(StringDecomposer::getPlainText)
									.toList()
					)
			);
			/* on */
		}
		return Joiner.on('\n').join(msgs);
	}

}
