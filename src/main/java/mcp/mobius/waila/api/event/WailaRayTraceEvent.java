package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.Accessor;
import net.minecraftforge.eventbus.api.Event;

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
