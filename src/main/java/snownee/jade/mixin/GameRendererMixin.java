package snownee.jade.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import snownee.jade.util.ClientPlatformProxy;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.AFTER), method = "render", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void jade$runTick(float f, long l, boolean bl, CallbackInfo ci, int i, int j, Window window, Matrix4f matrix4f, PoseStack poseStack, GuiGraphics guiGraphics) {
		ClientPlatformProxy.onRenderTick(guiGraphics);
	}

}
