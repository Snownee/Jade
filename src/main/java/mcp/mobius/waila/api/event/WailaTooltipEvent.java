package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.ITooltip;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired just before the Waila tooltip sizes are calculated. This is the last chance to make edits to
 * the information being displayed.
 * <p>
 * This event is not cancelable.
 * <p>
 * {@link #currentTip} - The current tooltip to be drawn.
 */
public class WailaTooltipEvent extends Event {

	private final ITooltip currentTip;
	private final Accessor<?> accessor;

	public WailaTooltipEvent(ITooltip currentTip, Accessor<?> accessor) {
		this.currentTip = currentTip;
		this.accessor = accessor;
	}

	public ITooltip getTooltip() {
		return currentTip;
	}

	public Accessor<?> getAccessor() {
		return accessor;
	}
}
