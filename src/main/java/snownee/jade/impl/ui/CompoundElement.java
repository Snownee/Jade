package snownee.jade.impl.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;

public class CompoundElement extends Element {

	protected final IElement large;
	protected final IElement small;

	public CompoundElement(IElement large, IElement small) {
		this.large = large;
		this.small = small;
	}

	@Override
	public Vec2 getSize() {
		return large.getSize();
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		Vec2 largeSize = large.getCachedSize();
		Vec2 smallSize = small.getCachedSize();
		large.render(guiGraphics, x, y, maxX, maxY);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 100);
		small.render(guiGraphics, x + largeSize.x - smallSize.x, y + largeSize.y - smallSize.y, maxX, maxY);
		guiGraphics.pose().popPose();
	}

}
