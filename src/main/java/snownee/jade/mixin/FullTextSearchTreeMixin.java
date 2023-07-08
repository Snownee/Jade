package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.searchtree.FullTextSearchTree;
import snownee.jade.JadeClient;

@Mixin(FullTextSearchTree.class)
public class FullTextSearchTreeMixin {

	@Inject(method = "refresh", at = @At("HEAD"))
	private <T> void jade$preRefresh(CallbackInfo ci) {
		JadeClient.hideModName = true;
	}

	@Inject(method = "refresh", at = @At("RETURN"))
	private <T> void jade$postRefresh(CallbackInfo ci) {
		JadeClient.hideModName = false;
	}

}
