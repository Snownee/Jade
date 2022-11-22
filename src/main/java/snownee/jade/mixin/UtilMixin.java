package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.Util;
import snownee.jade.Jade;

@Mixin(Util.class)
public class UtilMixin {

	@Inject(at = @At("TAIL"), method = "startTimerHackThread")
	private static void jade$loadComplete(CallbackInfo ci) {
		Jade.loadComplete();
	}

}
