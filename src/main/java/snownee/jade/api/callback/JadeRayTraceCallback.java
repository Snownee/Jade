package snownee.jade.api.callback;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.HitResult;
import snownee.jade.api.Accessor;

@FunctionalInterface
public interface JadeRayTraceCallback {

	@Nullable
	Accessor<?> onRayTrace(HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor);

}
