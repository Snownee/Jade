package snownee.jade.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.util.CommonProxy;

public interface ServerPayloadContext {
	default void execute(Runnable runnable) {
		CommonProxy.runWithContext(this, runnable);
	}

	default void sendPacket(CustomPacketPayload payload) {
		// Must go through CommonProxy, which sends via the Forge channel. Handing a payload straight
		// to a vanilla ClientboundCustomPayloadPacket works on NeoForge - payloads are registered in
		// vanilla's registry there - but on Forge the body then gets written by the wrong writer and
		// the client's codec reads a short buffer.
		CommonProxy.sendPacket(player(), payload);
	}

	ServerPlayer player();
}
