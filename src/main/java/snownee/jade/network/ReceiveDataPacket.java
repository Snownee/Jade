package snownee.jade.network;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.jade.Jade;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.ObjectDataCenter;

public record ReceiveDataPacket(CompoundTag tag) implements CustomPacketPayload {
	public static final int MAX_SIZE = 16 * 1024;
	public static final Type<ReceiveDataPacket> TYPE = new Type<>(JadeIds.PACKET_RECEIVE_DATA);
	public static final StreamCodec<FriendlyByteBuf, ReceiveDataPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG,
			ReceiveDataPacket::tag,
			ReceiveDataPacket::new
	);
	private static int spamCount;

	public static void handle(ReceiveDataPacket message, ClientPayloadContext context) {
		context.execute(() -> {
			ObjectDataCenter.setServerData(message.tag);
		});
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void send(CompoundTag tag, ServerPayloadContext context) {
		int size = tag.sizeInBytes();
		if (size > MAX_SIZE) {
			if (spamCount++ < 3) {
				Jade.LOGGER.error("Data size is too large: {}, max: {}, data: {}", size, MAX_SIZE, tag);
			}
			int c = 0;
			do {
				if (++c > 10) {
					return;
				}
				removeLargest(tag, 0, 1);
			} while (tag.sizeInBytes() > MAX_SIZE);
		}
		context.sendPacket(new ReceiveDataPacket(tag));
	}

	private static boolean removeLargest(CompoundTag tag, int depth, int maxDepth) {
		int largestSize = 0;
		String largestKey = null;
		Tag largestValue = null;
		for (String key : tag.getAllKeys()) {
			Tag childTag = Objects.requireNonNull(tag.get(key));
			int size = childTag.sizeInBytes();
			if (size > largestSize) {
				largestSize = size;
				largestKey = key;
				largestValue = childTag;
			}
		}
		if (largestKey == null) {
			return false;
		}
		if (depth < maxDepth && largestValue instanceof CompoundTag) {
			if (!removeLargest((CompoundTag) largestValue, depth + 1, maxDepth)) {
				tag.remove(largestKey);
			}
		} else {
			tag.remove(largestKey);
		}
		return true;
	}
}
