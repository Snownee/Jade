package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.api.ui.Size;

public class SpacerElement extends Element {

    private final Size dimension;

    public SpacerElement(Size dimension) {
        this.dimension = dimension;
    }

    @Override
    public Size getSize() {
        return dimension;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
    }
}
