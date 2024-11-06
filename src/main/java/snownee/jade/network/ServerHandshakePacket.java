package snownee.jade.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.Jade;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.ObjectDataCenter;

// This class of structure should not be changed
public record ServerHandshakePacket(
		String serverVersion,
		boolean connectable) implements CustomPacketPayload {
	public static final Type<ServerHandshakePacket> TYPE = new Type<>(JadeIds.PACKET_SERVER_HANDSHAKE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerHandshakePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			ServerHandshakePacket::serverVersion,
			ByteBufCodecs.BOOL,
			ServerHandshakePacket::connectable,
			ServerHandshakePacket::new);

	public static void handle(ServerHandshakePacket message, ClientPayloadContext context) {
		context.execute(() -> {
			if (message.connectable) {
				ObjectDataCenter.serverConnected = true;
				Jade.LOGGER.info("Connected from the server version {}", message.serverVersion);
			} else {
				Jade.LOGGER.warn("Failed connect from the server version {}", message.serverVersion);
			}
		});
	}

	@Override
	public Type<ServerHandshakePacket> type() {
		return TYPE;
	}
}
