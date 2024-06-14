package snownee.jade.mixin;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.jade.JadeClient;

@Mixin(SessionSearchTrees.class)
public class SessionSearchTreesMixin {

	@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private static void jade$preGet(
			Stream<ItemStack> stream,
			Item.TooltipContext tooltipContext,
			TooltipFlag tooltipFlag,
			CallbackInfoReturnable<Stream<String>> cir) {
		JadeClient.hideModNameIn(tooltipContext);
	}

}
