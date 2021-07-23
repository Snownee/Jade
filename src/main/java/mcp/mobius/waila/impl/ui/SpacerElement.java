package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.api.ui.Element;
import net.minecraft.world.phys.Vec2;

public class SpacerElement extends Element {

	private final Vec2 dimension;

	public SpacerElement(Vec2 dimension) {
		this.dimension = dimension;
	}

	@Override
	public Vec2 getSize() {
		return dimension;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
	}
}
