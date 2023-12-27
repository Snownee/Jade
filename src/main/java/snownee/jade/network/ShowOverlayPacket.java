package snownee.jade.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import snownee.jade.Jade;

public class ShowOverlayPacket {

	public final boolean show;

	public ShowOverlayPacket(boolean show) {
		this.show = show;
	}

	public static ShowOverlayPacket read(FriendlyByteBuf buffer) {
		return new ShowOverlayPacket(buffer.readBoolean());
	}

	public static void write(ShowOverlayPacket message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.show);
	}

	public static class Handler {

		public static void onMessage(ShowOverlayPacket message, Supplier<NetworkEvent.Context> context) {
			boolean show = message.show;
			Jade.LOGGER.info("Received request from the server to {} overlay", show ? "show" : "hide");
			context.get().enqueueWork(() -> {
				Jade.CONFIG.get().getGeneral().setDisplayTooltip(show);
				Jade.CONFIG.save();
			}).exceptionally(e -> {
				Jade.LOGGER.error("Failed to toggle overlay from the server");
				Jade.LOGGER.catching(e);
				return null;
			});
			context.get().setPacketHandled(true);
		}
	}
}
