package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.overlay.OverlayRenderer;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;)V", cancellable = true)
	private void jade$render(GuiGraphics guiGraphics, CallbackInfo ci) {
		BossBarOverlapMode mode = IWailaConfig.get().general().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.HIDE_BOSS_BAR && OverlayRenderer.shown) {
			ci.cancel();
		}
	}

}
