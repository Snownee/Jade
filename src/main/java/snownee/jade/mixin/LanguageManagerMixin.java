package snownee.jade.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.jade.util.JadeLanguages;

@Mixin(LanguageManager.class)
public class LanguageManagerMixin {
	@Inject(method = "onResourceManagerReload", at = @At("RETURN"))
	private void jade$onResourceManagerReload(
			ResourceManager resourceManager,
			CallbackInfo ci,
			@Local(name = "languageStack") List<String> languageStack) {
		JadeLanguages.INSTANCE.onResourceManagerReload(resourceManager, languageStack);
	}
}
