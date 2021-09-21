package mcp.mobius.waila.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import snownee.jade.client.ClientHandler;

import java.util.function.Supplier;

public class MessageBlockBreak {

	public MessageBlockBreak() {
	}

	public static MessageBlockBreak read(PacketBuffer buffer) {
		return new MessageBlockBreak();
	}

	public static void write(MessageBlockBreak message, PacketBuffer buffer) {
	}

	public static class Handler {
		public static void onMessage(MessageBlockBreak message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				ClientHandler.setProgressAlpha(1);
				ClientHandler.setSavedProgress(1);
			});
			context.get().setPacketHandled(true);
		}
	}

}
