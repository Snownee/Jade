package snownee.jade.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Util;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.SmoothChasingValue;

public abstract class SmoothScrollableList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {

	private static final float INERTIA_SPEED = 0.35F;
	private static final float INERTIA_DISTANCE_FACTOR = 60F;
	private static final float MAX_INERTIA_DISTANCE = 180F;
	private final SmoothChasingValue smoothScroll;
	private final SmoothChasingValue inertiaScroll;
	private boolean inertiaActive;
	private boolean contentDragging;
	private float dragVelocity;
	private long lastDragTime;

	public SmoothScrollableList(Minecraft client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
		smoothScroll = new SmoothChasingValue().withSpeed(0.6F);
		inertiaScroll = new SmoothChasingValue().withSpeed(INERTIA_SPEED);
	}

	@Override
	public void setScrollAmount(double scroll) {
		cancelInertia();
		if (ClientProxy.metadata.hasSmoothScroll()) {
			super.setScrollAmount(scroll);
		} else {
			smoothScroll.target(Math.clamp((float) scroll, 0F, (float) maxScrollAmount()));
		}
	}

	public void forceSetScrollAmount(double scroll) {
		cancelInertia();
		smoothScroll.start((float) scroll);
		super.setScrollAmount(scroll);
	}

	private void cancelInertia() {
		if (inertiaActive) {
			inertiaActive = false;
			smoothScroll.start((float) scrollAmount());
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		cancelInertia();
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		boolean result = super.mouseReleased(event);
		if (contentDragging) {
			contentDragging = false;
			long elapsed = Util.getMillis() - lastDragTime;
			float velocity = dragVelocity;
			dragVelocity = 0;
			if (elapsed <= 100 && Math.abs(velocity) > 0.001F) {
				float current = (float) scrollAmount();
				float distance = Math.clamp(velocity * INERTIA_DISTANCE_FACTOR, -MAX_INERTIA_DISTANCE, MAX_INERTIA_DISTANCE);
				float target = Math.clamp(current + distance, 0F, (float) maxScrollAmount());
				if (Math.abs(target - current) >= 1F) {
					inertiaScroll.start(current);
					inertiaScroll.target(target);
					inertiaActive = true;
				}
			}
		}
		return result;
	}

	@Override
	protected double scrollRate() {
		return defaultEntryHeight * (!ClientProxy.metadata.hasFastScroll() && JadeUI.hasControlDown() ? 9 : 3);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		cancelInertia();
		if (!scrolling) {
			E entry = getEntryAtPosition(event.x(), event.y());
			if (entry == null || !entry.isDragging()) {
				float current = (float) scrollAmount();
				float target = Math.clamp(current - (float) dy, 0F, (float) maxScrollAmount());
				long now = Util.getMillis();
				if (contentDragging) {
					long elapsed = now - lastDragTime;
					if (elapsed > 0) {
						float instant = (target - current) / elapsed;
						dragVelocity = dragVelocity * 0.4F + instant * 0.6F;
					}
				} else {
					dragVelocity = 0;
					contentDragging = true;
				}
				lastDragTime = now;
				smoothScroll.target(target);
				smoothScroll.value = target;
				super.setScrollAmount(target);
			} else {
				contentDragging = false;
				dragVelocity = 0;
			}
		} else {
			contentDragging = false;
			dragVelocity = 0;
		}
		return super.mouseDragged(event, dx, dy);
	}

	protected void tickSmoothScroll() {
		float deltaTicks = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
		if (inertiaActive) {
			float max = maxScrollAmount();
			inertiaScroll.target(Math.clamp(inertiaScroll.getTarget(), 0F, max));
			inertiaScroll.tick(deltaTicks);
			if (!inertiaScroll.isMoving()) {
				inertiaScroll.value = inertiaScroll.getTarget();
				inertiaActive = false;
			}
			super.setScrollAmount(Math.round(inertiaScroll.value));
			smoothScroll.start(inertiaScroll.value);
			return;
		}
		smoothScroll.tick(deltaTicks);
		if (!ClientProxy.metadata.hasSmoothScroll() && smoothScroll.isMoving()) {
			super.setScrollAmount(Math.round(smoothScroll.value));
		}
	}
}