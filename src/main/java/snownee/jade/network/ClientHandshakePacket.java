package snownee.jade.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import org.jetbrains.annotations.NotNull;

import snownee.jade.Jade;
import snownee.jade.api.JadeIds;
import snownee.jade.util.CommonProxy;

// This class of structure should not be changed
public record ClientHandshakePacket(String clientVersion) implements CustomPacketPayload {
	public static final Type<ClientHandshakePacket> TYPE = new Type<>(JadeIds.PACKET_CLIENT_HANDSHAKE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientHandshakePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			ClientHandshakePacket::clientVersion,
			ClientHandshakePacket::new);

	public static void handle(ClientHandshakePacket message, ServerPayloadContext context) {
		context.execute(() -> {
			Jade.LOGGER.info("{} try connect from protocol version {}", context.player().getScoreboardName(), message.clientVersion);
			CommonProxy.playerHandshake(message.clientVersion, context.player());
		});
	}

	@Override
	@NotNull
	public Type<ClientHandshakePacket> type() {
		return TYPE;
	}
}