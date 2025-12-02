package snownee.jade.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.Jade;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;

public record ShowOverlayPacket(boolean show) implements CustomPacketPayload {
	public static final Type<ShowOverlayPacket> TYPE = new Type<>(JadeIds.PACKET_SHOW_OVERLAY);
	public static final StreamCodec<RegistryFriendlyByteBuf, ShowOverlayPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			ShowOverlayPacket::show,
			ShowOverlayPacket::new
	);

	public static void handle(ShowOverlayPacket message, ClientPayloadContext context) {
		Jade.LOGGER.info("Received request from the server to {} overlay", message.show ? "show" : "hide");
		context.execute(() -> {
			IWailaConfig.get().general().setDisplayTooltip(message.show);
			IWailaConfig.get().save();
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
