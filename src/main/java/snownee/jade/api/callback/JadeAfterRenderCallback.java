package snownee.jade.api.callback;

import net.minecraft.client.gui.GuiGraphics;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.TooltipRect;

@FunctionalInterface
public interface JadeAfterRenderCallback {

	void afterRender(BoxElement rootElement, TooltipRect rect, GuiGraphics guiGraphics, Accessor<?> accessor);

}
