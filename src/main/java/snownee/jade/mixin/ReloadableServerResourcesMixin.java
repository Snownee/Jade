package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import snownee.jade.util.CommonProxy;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
	@Shadow
	@Final
	private RegistryAccess registryAccess;

	@Inject(method = "updateComponentsAndStaticRegistryTags", at = @At("TAIL"))
	private void jade$updateComponentsAndStaticRegistryTags(CallbackInfo ci) {
		CommonProxy.callComponentsBoundListeners(registryAccess);
	}
}
