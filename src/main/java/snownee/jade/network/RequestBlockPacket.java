package snownee.jade.network;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.WailaCommonRegistration;

public record RequestBlockPacket(
		BlockAccessorImpl.SyncData data,
		List<@Nullable IServerDataProvider<BlockAccessor>> dataProviders) implements CustomPacketPayload {
	public static final Type<RequestBlockPacket> TYPE = new Type<>(JadeIds.PACKET_REQUEST_BLOCK);
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestBlockPacket> CODEC = StreamCodec.composite(
			BlockAccessorImpl.SyncData.STREAM_CODEC,
			RequestBlockPacket::data,
			ByteBufCodecs.<ByteBuf, IServerDataProvider<BlockAccessor>>list()
					.apply(ByteBufCodecs.idMapper(
							$ -> Objects.requireNonNull(WailaCommonRegistration.instance().blockDataProviders.idMapper()).byId($),
							$ -> Objects.requireNonNull(WailaCommonRegistration.instance().blockDataProviders.idMapper()).getIdOrThrow($))),
			RequestBlockPacket::dataProviders,
			RequestBlockPacket::new);

	public static void handle(RequestBlockPacket message, ServerPayloadContext context) {
		BlockAccessorImpl.handleRequest(message, context, tag -> ReceiveDataPacket.send(tag, context));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
