package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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

	public static void handle(RequestTilePacket message, NetworkEvent.Context context) {
		BlockAccessorImpl.handleRequest(message.buffer, context.getSender(), context::enqueueWork, tag -> {
			CommonProxy.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new ReceiveDataPacket(tag));
		});
		context.setPacketHandled(true);
	}
}
