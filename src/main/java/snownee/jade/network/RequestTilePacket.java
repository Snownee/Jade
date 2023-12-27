package snownee.jade.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import snownee.jade.api.BlockAccessor;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.util.CommonProxy;

public class RequestTilePacket {

	public BlockAccessor accessor;
	public FriendlyByteBuf buffer;

	public RequestTilePacket(BlockAccessor accessor) {
		this.accessor = accessor;
	}

	public RequestTilePacket(FriendlyByteBuf buffer) {
		this.buffer = buffer;
	}

	public static RequestTilePacket read(FriendlyByteBuf buffer) {
		return new RequestTilePacket(buffer);
	}

	public static void write(RequestTilePacket message, FriendlyByteBuf buffer) {
		message.accessor.toNetwork(buffer);
	}

	public static class Handler {

		public static void onMessage(RequestTilePacket message, Supplier<NetworkEvent.Context> context) {
			BlockAccessorImpl.handleRequest(message.buffer, context.get().getSender(), $ -> {
				context.get().enqueueWork($).exceptionally(CommonProxy::crashAnyway);
			}, tag -> {
				CommonProxy.NETWORK.sendTo(new ReceiveDataPacket(tag), context.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
