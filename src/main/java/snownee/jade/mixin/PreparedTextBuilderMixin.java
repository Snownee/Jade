package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.JadeFont;

@Mixin(value = Font.PreparedTextBuilder.class, priority = 500)
public class PreparedTextBuilderMixin {
	@Shadow(aliases = {"field_24240", "b"}, remap = false)
	private Font this$0;

	@WrapOperation(method = "getShadowColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I"))
	private int jade$getShadowColor(int i, float f, Operation<Integer> original) {
		if (this$0.getClass() == JadeFont.class && IThemeHelper.get().isLightColorScheme()) {
			return IWailaConfig.Overlay.applyAlpha(i, 0.15F);
		}
		return original.call(i, f);
	}

	@Inject(
			method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/chat/Style;isBold()Z"),
			cancellable = true)
	private void jade$accept(int i, Style style, BakedGlyph bakedGlyph, CallbackInfoReturnable<Boolean> cir) {
		if (this$0.getClass() == JadeFont.class && JadeFont.isFilteredGlyph(bakedGlyph, this$0.lineHeight)) {
			cir.setReturnValue(false);
		}
	}

}
