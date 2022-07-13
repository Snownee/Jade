package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import snownee.jade.util.UsernameCache;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(at = @At("HEAD"), method = "addPlayer(ILnet/minecraft/client/player/AbstractClientPlayer;)V")
	private void jade$addPlayer(int i, AbstractClientPlayer player, CallbackInfo ci) {
		UsernameCache.setUsername(player.getUUID(), player.getGameProfile().getName());
	}
}
