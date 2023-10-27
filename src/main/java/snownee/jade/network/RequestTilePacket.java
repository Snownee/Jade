package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
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

	public static void handle(RequestTilePacket message, CustomPayloadEvent.Context context) {
		BlockAccessorImpl.handleRequest(message.buffer, context.getSender(), context::enqueueWork, tag -> {
			CommonProxy.NETWORK.send(new ReceiveDataPacket(tag), context.getConnection());
		});
		context.setPacketHandled(true);
	}
}
