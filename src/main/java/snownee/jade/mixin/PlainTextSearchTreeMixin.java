package snownee.jade.mixin;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.searchtree.PlainTextSearchTree;
import snownee.jade.JadeClient;

@Mixin(PlainTextSearchTree.class)
public interface PlainTextSearchTreeMixin {

	@Inject(method = "create", at = @At("HEAD"))
	private static <T> void jade$preCreate(List<T> list, Function<T, Stream<String>> function, CallbackInfoReturnable<PlainTextSearchTree<T>> cir) {
		JadeClient.hideModName = true;
	}

	@Inject(method = "create", at = @At("RETURN"))
	private static <T> void jade$postCreate(List<T> list, Function<T, Stream<String>> function, CallbackInfoReturnable<PlainTextSearchTree<T>> cir) {
		JadeClient.hideModName = false;
	}

}
