package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import snownee.jade.JadeClient;

@Mixin(I18n.class)
public class I18nMixin {

	@Inject(at = @At("TAIL"), method = "setLanguage")
	private static void jade$setLanguage(Language language, CallbackInfo ci) {
		JadeClient.checkTranslations();
	}

}
