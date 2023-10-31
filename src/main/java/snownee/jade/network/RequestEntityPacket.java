package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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

	public static void handle(final RequestEntityPacket message, NetworkEvent.Context context) {
		EntityAccessorImpl.handleRequest(message.buffer, context.getSender(), context::enqueueWork, tag -> {
			CommonProxy.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new ReceiveDataPacket(tag));
		});
		context.setPacketHandled(true);
	}
}
