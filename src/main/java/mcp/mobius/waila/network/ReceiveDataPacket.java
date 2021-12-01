package mcp.mobius.waila.network;

import java.util.function.Supplier;

import mcp.mobius.waila.impl.ObjectDataCenter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ReceiveDataPacket {

	public CompoundTag tag;

	public ReceiveDataPacket(CompoundTag tag) {
		this.tag = tag;
	}

	public static ReceiveDataPacket read(FriendlyByteBuf buffer) {
		return new ReceiveDataPacket(buffer.readNbt());
	}

	public static void write(ReceiveDataPacket message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.tag);
	}

	public static class Handler {

		public static void onMessage(ReceiveDataPacket message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				ObjectDataCenter.setServerData(message.tag);
			});
			context.get().setPacketHandled(true);
		}
	}
}
