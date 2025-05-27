package snownee.jade.impl.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.ui.ScreenDirection;

public class SlimProgressStyle extends ProgressStyle {

	@Override
	public ProgressStyle textColor(int color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProgressStyle direction(ScreenDirection direction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float w, float h, float progress, Component text) {
//		if (overlay != null) {
//			Vec2 size = new Vec2(w * progress, h);
//			overlay.size(size);
//			overlay.render(guiGraphics, x, y, size.x, size.y);
//		} else {
//			DisplayHelper.INSTANCE.drawGradientProgress(guiGraphics, x, y, w, h, progress, color);
//		}
	}

}
