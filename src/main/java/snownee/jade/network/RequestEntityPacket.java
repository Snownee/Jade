package snownee.jade.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.EntityAccessorImpl;

public record RequestEntityPacket(EntityAccessorImpl.SyncData data) implements CustomPacketPayload {
	public static final Type<RequestEntityPacket> TYPE = new Type<>(Identifiers.PACKET_REQUEST_ENTITY);
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestEntityPacket> CODEC = CustomPacketPayload.codec(RequestEntityPacket::write, RequestEntityPacket::read);

	public static RequestEntityPacket read(RegistryFriendlyByteBuf buffer) {
		return new RequestEntityPacket(new EntityAccessorImpl.SyncData(buffer));
	}

	public static void handle(RequestEntityPacket message, ServerPayloadContext context) {
		EntityAccessorImpl.handleRequest(message.data, context, tag -> {
			context.sendPacket(new ReceiveDataPacket(tag));
		});
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		data.write(buffer);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
