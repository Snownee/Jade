package snownee.jade.api.callback;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.TooltipAnimation;

@FunctionalInterface
public interface JadeAfterRenderCallback {

	void afterRender(BoxElement root, TooltipAnimation animation, GuiGraphicsExtractor graphics, Accessor<?> accessor);

}
