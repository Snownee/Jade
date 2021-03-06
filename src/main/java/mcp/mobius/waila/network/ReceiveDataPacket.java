package mcp.mobius.waila.network;

import java.util.function.Supplier;

import mcp.mobius.waila.impl.ObjectDataCenter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ReceiveDataPacket {

	public CompoundNBT tag;

	public ReceiveDataPacket(CompoundNBT tag) {
		this.tag = tag;
	}

	public static ReceiveDataPacket read(PacketBuffer buffer) {
		return new ReceiveDataPacket(buffer.readCompoundTag());
	}

	public static void write(ReceiveDataPacket message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.tag);
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
