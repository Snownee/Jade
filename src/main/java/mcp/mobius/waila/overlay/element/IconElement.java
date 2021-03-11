package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.overlay.DisplayUtil;
import mcp.mobius.waila.overlay.IconUI;

public class IconElement extends Element {

    private final IconUI icon;
    private final int size = 8;

    public IconElement(IconUI icon) {
        this.icon = icon;
    }

    @Override
    public Size getSize() {
        return new Size(size, size);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        DisplayUtil.renderIcon(matrixStack, x, y, size, size, icon);
    }

}
