package snownee.jade.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphInfo;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.JadeFont;

@Mixin(Font.StringRenderOutput.class)
public class StringRenderOutputMixin {
	@Final
	@Shadow
	Font this$0;

	@Final
	@Mutable
	@Shadow
	private float dimFactor;
	@Final
	@Mutable
	@Shadow
	private float r;
	@Final
	@Mutable
	@Shadow
	private float g;
	@Final
	@Mutable
	@Shadow
	private float b;
	@Final
	@Mutable
	@Shadow
	private float a;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void jade$init(
			Font font,
			MultiBufferSource multiBufferSource,
			float f,
			float g,
			int i,
			boolean bl,
			Matrix4f matrix4f,
			Font.DisplayMode displayMode,
			int j,
			CallbackInfo ci) {
		if (bl && this$0.getClass() == JadeFont.class && IThemeHelper.get().isLightColorScheme()) {
			dimFactor = 1;
			this.r = (float) (i >> 16 & 0xFF) / 255.0f;
			this.g = (float) (i >> 8 & 0xFF) / 255.0f;
			this.b = (float) (i & 0xFF) / 255.0f;
			this.a = (float) (i >> 24 & 0xFF) / 255.0f * 0.15f;
		}
	}


	@Inject(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;isObfuscated()Z"), cancellable = true)
	private void jade$accept(
			int pPositionInCurrentSequence,
			Style pStyle,
			int pCodePoint,
			CallbackInfoReturnable<Boolean> cir,
			@Local GlyphInfo glyphInfo) {
		if (this$0.getClass() == JadeFont.class && JadeFont.isTooLarge(glyphInfo, 9)) {
			cir.setReturnValue(false);
		}
	}
}
