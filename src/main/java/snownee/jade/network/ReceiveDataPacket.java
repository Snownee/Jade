package snownee.jade.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import snownee.jade.impl.ObjectDataCenter;

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

	public static void handle(ReceiveDataPacket message, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ObjectDataCenter.setServerData(message.tag);
		});
		context.setPacketHandled(true);
	}
}
