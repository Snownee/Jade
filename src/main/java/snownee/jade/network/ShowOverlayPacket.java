package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import snownee.jade.Jade;
import snownee.jade.api.Identifiers;

public record ShowOverlayPacket(boolean show) implements CustomPacketPayload {

	public static ShowOverlayPacket read(FriendlyByteBuf buffer) {
		return new ShowOverlayPacket(buffer.readBoolean());
	}

	public static void handle(ShowOverlayPacket message, PlayPayloadContext context) {
		boolean show = message.show;
		Jade.LOGGER.info("Received request from the server to {} overlay", show ? "show" : "hide");
		context.workHandler().execute(() -> {
			Jade.CONFIG.get().getGeneral().setDisplayTooltip(show);
			Jade.CONFIG.save();
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(show);
	}

	@Override
	public ResourceLocation id() {
		return Identifiers.PACKET_SHOW_OVERLAY;
	}
}
