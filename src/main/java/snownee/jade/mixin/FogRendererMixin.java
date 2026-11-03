package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import snownee.jade.JadeClient;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void jade$setupFog(
			Camera camera,
			int renderDistanceInChunks,
			DeltaTracker deltaTracker,
			float darkenWorldAmount,
			ClientLevel level,
			boolean shouldCreateBossFog,
			CallbackInfoReturnable<FogData> cir) {
		FogData fog = cir.getReturnValue();
		JadeClient.renderDistanceStart = fog.renderDistanceStart;
		JadeClient.renderDistanceEnd = fog.renderDistanceEnd;
	}
}
