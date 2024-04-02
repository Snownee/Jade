package snownee.jade.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.EntityAccessorImpl;

public record RequestEntityPacket(EntityAccessorImpl.SyncData data) implements CustomPacketPayload {
	public static final Type<RequestEntityPacket> TYPE = new Type<>(Identifiers.PACKET_REQUEST_ENTITY);
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestEntityPacket> CODEC = StreamCodec.composite(
			EntityAccessorImpl.SyncData.STREAM_CODEC,
			RequestEntityPacket::data,
			RequestEntityPacket::new
	);

	public static void handle(RequestEntityPacket message, ServerPayloadContext context) {
		EntityAccessorImpl.handleRequest(message.data, context, tag -> {
			context.sendPacket(new ReceiveDataPacket(tag));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
