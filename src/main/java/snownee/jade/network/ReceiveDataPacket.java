package snownee.jade.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.ObjectDataCenter;

public record ReceiveDataPacket(CompoundTag tag) implements CustomPacketPayload {

	public static ReceiveDataPacket read(FriendlyByteBuf buffer) {
		return new ReceiveDataPacket(buffer.readNbt());
	}

	public static void handle(ReceiveDataPacket message, PlayPayloadContext context) {
		context.workHandler().execute(() -> {
			ObjectDataCenter.setServerData(message.tag);
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(tag);
	}

	@Override
	public ResourceLocation id() {
		return Identifiers.PACKET_RECEIVE_DATA;
	}
}
