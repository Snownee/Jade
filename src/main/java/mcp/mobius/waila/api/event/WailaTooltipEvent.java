package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.IAccessor;
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
    private final IAccessor accessor;

    public WailaTooltipEvent(ITooltip currentTip, IAccessor accessor) {
        this.currentTip = currentTip;
        this.accessor = accessor;
    }

    public ITooltip getTooltip() {
        return currentTip;
    }

    public IAccessor getAccessor() {
        return accessor;
    }
}
