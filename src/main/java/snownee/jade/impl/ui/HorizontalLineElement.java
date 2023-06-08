package snownee.jade.impl.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.util.Color;

public class HorizontalLineElement extends Element {

	public int color = OverlayRenderer.normalTextColorRaw;

	@Override
	public Vec2 getSize() {
		return new Vec2(10, 4);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		y = (y + maxY - 0.5F) / 2;
		DisplayHelper.fill(guiGraphics, x + 2, y, maxX - 2, y + 0.5F, color);
		x += 0.5F;
		y += 0.5F;
		maxX += 0.5F;
		var shadow = Color.rgb(color);
		shadow = Color.rgb(shadow.getRed() / 4, shadow.getGreen() / 4, shadow.getBlue() / 4, shadow.getOpacity());
		DisplayHelper.fill(guiGraphics, x + 2, y, maxX - 2, y + 0.5F, shadow.toInt());
	}

}
