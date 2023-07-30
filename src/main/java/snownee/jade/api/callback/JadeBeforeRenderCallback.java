package snownee.jade.api.callback;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.theme.Theme;

@FunctionalInterface
public interface JadeBeforeRenderCallback {

	class ColorSetting {
		public float alpha;
		public MutableObject<Theme> theme;
	}

	boolean beforeRender(ITooltip tooltip, Rect2i rect, GuiGraphics guiGraphics, Accessor<?> accessor, ColorSetting color);

}
