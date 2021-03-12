package mcp.mobius.waila.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IElement;
import mcp.mobius.waila.api.IElement.Align;
import mcp.mobius.waila.api.IElementHelper;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.util.ResourceLocation;

public class Tooltip implements ITooltip {

    public static class Line {
        private final List<IElement> left = new ArrayList<>();
        private final List<IElement> right = new ArrayList<>();
        private Size size;

        public List<IElement> getAlignedElements(Align align) {
            return align == Align.LEFT ? left : right;
        }

        public Size getSize() {
            if (size == null) {
                int width = 0, height = 0;
                for (IElement element : left) {
                    Size elementSize = element.getCachedSize();
                    width += elementSize.width;
                    height = Math.max(height, elementSize.height);
                }
                for (IElement element : right) {
                    Size elementSize = element.getCachedSize();
                    width += elementSize.width;
                    height = Math.max(height, elementSize.height);
                }
                size = new Size(width, height);
            }
            return size;
        }

        public void render(MatrixStack matrixStack, int x, int y, int maxWidth, int maxHeight) {
            int ox = maxWidth, oy = y;
            for (int i = right.size() - 1; i >= 0; i--) {
                IElement element = right.get(i);
                Size translate = element.getTranslation();
                Size size = element.getCachedSize();
                ox -= size.width;
                drawBorder(matrixStack, ox, oy, element);
                element.render(matrixStack, ox + translate.width, oy + translate.height, x + maxWidth, y + maxHeight);
            }
            maxWidth = ox;
            ox = x;
            for (IElement element : left) {
                Size translate = element.getTranslation();
                Size size = element.getCachedSize();
                drawBorder(matrixStack, ox, oy, element);
                element.render(matrixStack, ox + translate.width, oy + translate.height, maxWidth, y + maxHeight);
                ox += size.width;
            }
        }

        public void drawBorder(MatrixStack matrixStack, int x, int y, IElement element) {
            if (Waila.CONFIG.get().getGeneral().isDebug()) {
                Size translate = element.getTranslation();
                Size size = element.getCachedSize();
                DisplayUtil.drawBorder(matrixStack, x, y, x + size.width, y + size.height, 0xFFFF0000);
                if (translate != Size.ZERO) {
                    DisplayUtil.drawBorder(matrixStack, x + translate.width, y + translate.height, x + translate.width + size.width, y + translate.height + size.height, 0xFF0000FF);
                }
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

}
