package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.IAccessor;
import net.minecraftforge.eventbus.api.Event;

public class WailaRayTraceEvent extends Event {

	private IAccessor target;
	private final IAccessor originalTarget;

	public WailaRayTraceEvent(IAccessor target) {
		this.target = originalTarget = target;
	}

	public IAccessor getOriginalTarget() {
		return originalTarget;
	}

	public IAccessor getTarget() {
		return target;
	}

	public void setTarget(IAccessor target) {
		this.target = target;
	}

}
