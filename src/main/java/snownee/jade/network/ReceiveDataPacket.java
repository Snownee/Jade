package snownee.jade.network;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.ObjectDataCenter;

public record ReceiveDataPacket(CompoundTag tag) implements CustomPacketPayload {
	public static final Type<ReceiveDataPacket> TYPE = new Type<>(Identifiers.PACKET_RECEIVE_DATA);
	public static final StreamCodec<FriendlyByteBuf, ReceiveDataPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG,
			ReceiveDataPacket::tag,
			ReceiveDataPacket::new
	);

	public static void handle(ReceiveDataPacket message, ClientPayloadContext context) {
		context.execute(() -> {
			ObjectDataCenter.setServerData(message.tag);
		});
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
