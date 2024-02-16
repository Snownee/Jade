package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.Jade;
import snownee.jade.api.Identifiers;

public record ShowOverlayPacket(boolean show) implements CustomPacketPayload {
	public static final Type<ShowOverlayPacket> TYPE = new Type<>(Identifiers.PACKET_SHOW_OVERLAY);
	public static final StreamCodec<RegistryFriendlyByteBuf, ShowOverlayPacket> CODEC = CustomPacketPayload.codec(ShowOverlayPacket::write, ShowOverlayPacket::read);

	public static ShowOverlayPacket read(FriendlyByteBuf buffer) {
		return new ShowOverlayPacket(buffer.readBoolean());
	}

	public static void handle(ShowOverlayPacket message, ClientPayloadContext context) {
		Jade.LOGGER.info("Received request from the server to {} overlay", message.show ? "show" : "hide");
		context.execute(() -> {
			Jade.CONFIG.get().getGeneral().setDisplayTooltip(message.show);
			Jade.CONFIG.save();
		});
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(show);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
