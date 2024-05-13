package snownee.jade.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
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
		message.accessor = null;
	}

	public static class Handler {

		public static void onMessage(final RequestEntityPacket message, Supplier<NetworkEvent.Context> context) {
			EntityAccessorImpl.handleRequest(message.buffer, context.get().getSender(), $ -> {
				context.get().enqueueWork($).exceptionally(CommonProxy::crashAnyway);
			}, tag -> {
				CommonProxy.NETWORK.sendTo(new ReceiveDataPacket(tag), context.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
