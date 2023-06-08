package snownee.jade.impl.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.IconUI;

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
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		DisplayHelper.renderIcon(guiGraphics, x, y, size, size, icon);
	}

}
