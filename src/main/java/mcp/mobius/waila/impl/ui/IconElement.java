package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.world.phys.Vec2;

public class IconElement extends Element {

	private final IconUI icon;
	private final int size = 8;

	public IconElement(IconUI icon) {
		this.icon = icon;
	}

	@Override
	public Vec2 getSize() {
		return new Vec2(size, size);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		DisplayHelper.renderIcon(matrixStack, x, y, size, size, icon);
	}

}
