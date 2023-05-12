package snownee.jade.api.callback;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;

@FunctionalInterface
public interface JadeBeforeRenderCallback {

	public static class ColorSetting {
		public float alpha;
		public int backgroundColor;
		public int gradientStart;
		public int gradientEnd;
	}

	boolean beforeRender(ITooltip tooltip, Rect2i rect, GuiGraphics guiGraphics, Accessor<?> accessor, ColorSetting color);

}
