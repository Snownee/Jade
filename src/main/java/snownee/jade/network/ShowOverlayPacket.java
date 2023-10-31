package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
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

	public static void handle(ShowOverlayPacket message, NetworkEvent.Context context) {
		boolean show = message.show;
		Jade.LOGGER.info("Received request from the server to {} overlay", show ? "show" : "hide");
		context.enqueueWork(() -> {
			Jade.CONFIG.get().getGeneral().setDisplayTooltip(show);
			Jade.CONFIG.save();
		});
		context.setPacketHandled(true);
	}
}
