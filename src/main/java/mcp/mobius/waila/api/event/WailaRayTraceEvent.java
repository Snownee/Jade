package mcp.mobius.waila.api.event;

import javax.annotation.Nullable;

import mcp.mobius.waila.api.Accessor;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired after the Waila's ray-casting is done. You can modify the target result or
 * return null to cancel this process.
 */
public class WailaRayTraceEvent extends Event {

	private Accessor<?> accessor;
	private final Accessor<?> originalAccessor;
	private final HitResult hitResult;

	public WailaRayTraceEvent(Accessor<?> accessor, HitResult hitResult) {
		this.accessor = originalAccessor = accessor;
		this.hitResult = hitResult;
	}

	@Nullable
	public Accessor<?> getOriginalAccessor() {
		return originalAccessor;
	}

	@Nullable
	public Accessor<?> getAccessor() {
		return accessor;
	}

	public void setAccessor(@Nullable Accessor<?> accessor) {
		this.accessor = accessor;
	}

	public HitResult getHitResult() {
		return hitResult;
	}

}
