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
			Jade.LOGGER.info("{} try connect from the client version {}", context.player().getScoreboardName(), message.clientVersion);
			CommonProxy.playerHandshake(ClientHandshakePacket.getVersionInt(message.clientVersion), context.player());
		});
	}

	@Override
	public Type<ClientHandshakePacket> type() {
		return TYPE;
	}

	public static int getVersionInt(@NotNull String version) {
		String[] versions = version.split("\\+")[0].split("-")[0].split("\\.");
		return Integer.parseInt(versions[0]) * 10000 + Integer.parseInt(versions[1]) * 100 + Integer.parseInt(versions[2]);
	}
}