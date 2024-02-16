package snownee.jade.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.BlockAccessorImpl;

public record RequestBlockPacket(BlockAccessorImpl.SyncData data) implements CustomPacketPayload {
	public static final Type<RequestBlockPacket> TYPE = new Type<>(Identifiers.PACKET_REQUEST_BLOCK);
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestBlockPacket> CODEC = CustomPacketPayload.codec(RequestBlockPacket::write, RequestBlockPacket::read);

	public static RequestBlockPacket read(RegistryFriendlyByteBuf buffer) {
		return new RequestBlockPacket(new BlockAccessorImpl.SyncData(buffer));
	}

	public static void handle(RequestBlockPacket message, ServerPayloadContext context) {
		BlockAccessorImpl.handleRequest(message.data, context, tag -> {
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
