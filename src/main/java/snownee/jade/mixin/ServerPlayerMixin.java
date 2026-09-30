package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.server.level.ServerPlayer;
import snownee.jade.Jade;
import snownee.jade.util.JadeServerPlayer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements JadeServerPlayer {

	@Unique
	private boolean jade$isConnected;
	@Unique
	private long jade$lastRequestNanos;

	@Override
	public boolean jade$isConnected() {
		return jade$isConnected;
	}

	@Override
	public void jade$setConnected(boolean connected) {
		this.jade$isConnected = connected;
	}

	@Override
	public boolean jade$tryAcquireRequest() {
		long now = System.nanoTime();
		if (now - jade$lastRequestNanos < Jade.REQUEST_INTERVAL_NANOS) {
			return false;
		}
		jade$lastRequestNanos = now;
		return true;
	}
}
