package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.util.math.vector.Vector2f;

public class IconElement extends Element {

	private final IconUI icon;
	private final int size = 8;

	public IconElement(IconUI icon) {
		this.icon = icon;
	}

	@Override
	public Vector2f getSize() {
		return new Vector2f(size, size);
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		DisplayHelper.renderIcon(matrixStack, x, y, size, size, icon);
	}

}
