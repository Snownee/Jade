package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import snownee.jade.api.EntityAccessor;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.util.CommonProxy;

public class RequestEntityPacket {

	public EntityAccessor accessor;
	public FriendlyByteBuf buffer;

	public RequestEntityPacket(EntityAccessor accessor) {
		this.accessor = accessor;
	}

	public RequestEntityPacket(FriendlyByteBuf buffer) {
		this.buffer = buffer;
	}

	public static RequestEntityPacket read(FriendlyByteBuf buffer) {
		return new RequestEntityPacket(buffer);
	}

	public static void write(RequestEntityPacket message, FriendlyByteBuf buffer) {
		message.accessor.toNetwork(buffer);
	}

	public static void handle(final RequestEntityPacket message, CustomPayloadEvent.Context context) {
		EntityAccessorImpl.handleRequest(message.buffer, context.getSender(), context::enqueueWork, tag -> {
			CommonProxy.NETWORK.send(new ReceiveDataPacket(tag), context.getConnection());
		});
		context.setPacketHandled(true);
	}
}
