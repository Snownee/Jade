package snownee.jade.network;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.util.CommonProxy;

public interface ServerPayloadContext {
	default void execute(Runnable runnable) {
		CommonProxy.runWithContext(this, runnable);
	}

	default void sendPacket(CustomPacketPayload payload) {
		player().connection.send(new ClientboundCustomPayloadPacket(payload));
	}

	ServerPlayer player();
}
