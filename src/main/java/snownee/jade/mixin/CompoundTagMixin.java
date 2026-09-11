package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.CrashReport;
import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.AccessorImpl;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin {
	@WrapOperation(
			method = "readNamedTagData",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/CrashReport;forThrowable(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/CrashReport;"))
	private static CrashReport jade$throwInsteadOfCrashReport(Throwable cause, String description, Operation<CrashReport> original) {
		if (AccessorImpl.jade$isInJadeDecode()) {
			throw sneakyThrow(cause);
		}
		return original.call(cause, description);
	}

	@Unique
	@SuppressWarnings("unchecked")
	private static <E extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws E {
		throw (E) throwable;
	}
}