package snownee.jade.api.callback;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.phys.HitResult;
import snownee.jade.api.Accessor;

/**
 * Allows addons to replace the accessor generated from a hit result.
 */
@FunctionalInterface
public interface JadeRayTraceCallback {

	/**
	 * Called after ray tracing resolves a target.
	 *
	 * @param hitResult the raw hit result
	 * @param accessor the accessor Jade would use
	 * @param originalAccessor the unmodified accessor
	 * @return a replacement accessor, or {@code null} to keep the current one
	 */
	@Nullable
	Accessor<?> onRayTrace(HitResult hitResult, Accessor<?> accessor, Accessor<?> originalAccessor);

}
