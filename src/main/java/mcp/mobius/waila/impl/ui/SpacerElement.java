package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import net.minecraft.util.math.vector.Vector2f;

public class SpacerElement extends Element {

	private final Vector2f dimension;

	public SpacerElement(Vector2f dimension) {
		this.dimension = dimension;
	}

	@Override
	public Vector2f getSize() {
		return dimension;
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
	}
}
