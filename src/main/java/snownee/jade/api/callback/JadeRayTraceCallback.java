package snownee.jade.api.callback;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.phys.HitResult;
import snownee.jade.api.Accessor;

@FunctionalInterface
public interface JadeRayTraceCallback {

	@Nullable
	Accessor<?> onRayTrace(HitResult hitResult, Accessor<?> accessor, Accessor<?> originalAccessor);

}
