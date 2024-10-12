package snownee.jade.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;

@Mixin(Font.StringRenderOutput.class)
public class StringRenderOutputMixin {

	@Mutable
	@Shadow
	@Final
	private float dimFactor;
	@Mutable
	@Shadow
	@Final
	private int color;

	@Inject(
			method = "<init>(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZLorg/joml/Matrix4f;Lnet/minecraft/client/gui/Font$DisplayMode;I)V",
			at = @At("RETURN"))
	private void jade$init(
			Font font,
			MultiBufferSource multiBufferSource,
			float f,
			float g,
			int i,
			int j,
			boolean bl,
			Matrix4f matrix4f,
			Font.DisplayMode displayMode,
			int k,
			CallbackInfo ci) {
		if (bl && DisplayHelper.enableBetterTextShadow() && IThemeHelper.get().isLightColorScheme()) {
			dimFactor = 1;
			color = IWailaConfig.Overlay.applyAlpha(i, 0.15F);
		}
	}

}
