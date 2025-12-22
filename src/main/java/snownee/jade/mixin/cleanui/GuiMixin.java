package snownee.jade.mixin.cleanui;

import org.joml.Vector2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import snownee.jade.JadeClient;
import snownee.jade.api.gaze.GazePoint;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.impl.WailaClientRegistration;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
	private void jade$renderHotbarAndDecorations(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		GazePoint gazePoint = WailaClientRegistration.instance().gazePoint;
		if (!gazePoint.canUse()) {
			return;
		}
		Vector2fc pos = gazePoint.pos();
		if (pos == null) {
			return;
		}
		Rect2f hotbarArea = JadeClient.hotbarArea(graphics);
		if (!hotbarArea.contains(pos.x(), pos.y())) {
			ci.cancel();
		}
	}
}
