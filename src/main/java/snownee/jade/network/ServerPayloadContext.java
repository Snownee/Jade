package snownee.jade.network;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPayloadContext {
	default void execute(Runnable runnable) {
		player().level().getServer().execute(() -> PacketContext.runWithContext(this.player(), runnable));
	}

	default void sendPacket(CustomPacketPayload payload) {
		player().connection.send(new ClientboundCustomPayloadPacket(payload));
	}

	ServerPlayer player();
}
