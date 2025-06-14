package snownee.jade.api.ui;

import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum MessageType {
	NORMAL, INFO, TITLE, SUCCESS, WARNING, DANGER, FAILURE;

	public static final IntFunction<MessageType> BY_ID = ByIdMap.continuous(
			MessageType::ordinal,
			values(),
			ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, MessageType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MessageType::ordinal);

	public static MessageType parse(String s) {
		try {
			return valueOf(s);
		} catch (Exception ignored) {
			return NORMAL;
		}
	}
}
