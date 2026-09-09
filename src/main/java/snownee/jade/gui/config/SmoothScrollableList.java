package snownee.jade.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.SmoothChasingValue;

public abstract class SmoothScrollableList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {

	private final SmoothChasingValue smoothScroll;

	public SmoothScrollableList(Minecraft client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
		smoothScroll = new SmoothChasingValue().withSpeed(0.6F);
	}

	@Override
	public void setScrollAmount(double scroll) {
		if (ClientProxy.metadata.hasSmoothScroll()) {
			super.setScrollAmount(scroll);
		} else {
			smoothScroll.target(Mth.clamp((float) scroll, 0, maxScrollAmount()));
		}
	}

	public void forceSetScrollAmount(double scroll) {
		smoothScroll.start((float) scroll);
		super.setScrollAmount(scroll);
	}

	@Override
	protected double scrollRate() {
		return defaultEntryHeight * (!ClientProxy.metadata.hasFastScroll() && JadeUI.hasControlDown() ? 9 : 3);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (!scrolling) {
			E entry = getEntryAtPosition(event.x(), event.y());
			if (entry == null || !entry.isDragging()) {
				smoothScroll.target(Mth.clamp((float) (scrollAmount() - dy), 0, maxScrollAmount()));
			}
		}
		smoothScroll.value = smoothScroll.getTarget();
		super.setScrollAmount(smoothScroll.value);
		return super.mouseDragged(event, dx, dy);
	}

	protected void tickSmoothScroll() {
		float deltaTicks = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
		smoothScroll.tick(deltaTicks);
		if (!ClientProxy.metadata.hasSmoothScroll() && smoothScroll.isMoving()) {
			super.setScrollAmount(Math.round(smoothScroll.value));
		}
	}
}