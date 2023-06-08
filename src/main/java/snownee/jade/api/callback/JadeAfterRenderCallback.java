package snownee.jade.api.callback;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;

@FunctionalInterface
public interface JadeAfterRenderCallback {

	void afterRender(ITooltip tooltip, Rect2i rect, GuiGraphics guiGraphics, Accessor<?> accessor);

}
