package snownee.jade.mixin;

import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import snownee.jade.util.IJadeServerPlayer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IJadeServerPlayer {

	@Unique
	private String jade$clientVersion = null;

	@Unique
	private boolean jade$isConnected = false;

	@Override
	public String jade$getClientVersion() {
		return jade$clientVersion;
	}

	@Override
	public boolean jade$isConnected() {
		return jade$isConnected;
	}

	@Override
	public void jade$setClientVersion(String version) {
		this.jade$clientVersion = version;
	}

	@Override
	public void jade$setConnected(boolean is) {
		this.jade$isConnected = is;
	}
}
