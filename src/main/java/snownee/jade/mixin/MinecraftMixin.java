package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import snownee.jade.util.ClientPlatformProxy;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER
			), method = "runTick"
	)
	private void jade$runTick(boolean bl, CallbackInfo ci) {
		ClientPlatformProxy.onRenderTick();
	}

}
