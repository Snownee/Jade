package snownee.jade.mixin;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import snownee.jade.JadeClient;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Inject(
			method = "setupFog", at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;getDevice()Lcom/mojang/blaze3d/systems/GpuDevice;"))
	private void jade$setupFog(
			Camera camera,
			int i,
			boolean bl,
			DeltaTracker deltaTracker,
			float f,
			ClientLevel clientLevel,
			CallbackInfoReturnable<Vector4f> cir,
			@Local FogData fogData) {
		JadeClient.renderDistanceStart = fogData.renderDistanceStart;
		JadeClient.renderDistanceEnd = fogData.renderDistanceEnd;
	}
}
