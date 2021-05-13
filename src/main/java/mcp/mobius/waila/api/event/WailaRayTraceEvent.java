package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.Accessor;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired after the Waila's ray-tracing is done. You can modify the target result or
 * return null to cancel this process.
 */
public class WailaRayTraceEvent extends Event {

	private Accessor target;
	private final Accessor originalTarget;

	public WailaRayTraceEvent(Accessor target) {
		this.target = originalTarget = target;
	}

	public Accessor getOriginalTarget() {
		return originalTarget;
	}

	public Accessor getTarget() {
		return target;
	}

	public void setTarget(Accessor target) {
		this.target = target;
	}

}
