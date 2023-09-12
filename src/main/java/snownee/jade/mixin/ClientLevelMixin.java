package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import snownee.jade.util.UsernameCache;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(at = @At("HEAD"), method = "addEntity")
	private void jade$addEntity(Entity entity, CallbackInfo ci) {
		if (entity instanceof Player player) {
			UsernameCache.setUsername(player.getUUID(), player.getGameProfile().getName());
		}
	}
}
