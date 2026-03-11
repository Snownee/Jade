package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.util.ClientProxy;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {

	@Inject(at = @At("HEAD"), method = "extractRenderState", cancellable = true)
	private void jade$render(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		BossBarOverlapMode mode = IWailaConfig.get().general().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.HIDE_BOSS_BAR && OverlayRenderer.shown) {
			ci.cancel();
		}
	}

	@Inject(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I"),
			method = "extractRenderState")
	private void jade$captureHeight(
			GuiGraphicsExtractor graphics,
			CallbackInfo ci,
			@Local(name = "event") LerpingBossEvent event,
			@Local(name = "yOffset") int yOffset) {
		ClientProxy.drawBossBarPost(event, yOffset);
	}

}
