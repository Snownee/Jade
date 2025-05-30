package snownee.jade.api.callback;

import net.minecraft.client.gui.GuiGraphics;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.TooltipAnimation;

@FunctionalInterface
public interface JadeBeforeRenderCallback {

	boolean beforeRender(BoxElement root, TooltipAnimation animation, GuiGraphics graphics, Accessor<?> accessor);

}
