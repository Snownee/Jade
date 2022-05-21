package snownee.jade.api.callback;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.HitResult;
import snownee.jade.api.Accessor;

/**
 * This event is fired after the Waila's ray-casting is done. You can modify the target result or
 * return null to cancel this process.
 */
public interface JadeRayTraceCallback {

	@Nullable
	Accessor<?> onRayTrace(HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor);

}
