package snownee.jade.network;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import snownee.jade.Jade;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.JadeCodecs;

public record ServerHandshakePacket(
		Map<Identifier, Object> serverConfig,
		List<Block> shearableBlocks,
		List<Identifier> blockProviderIds,
		List<Identifier> entityProviderIds) implements CustomPacketPayload {
	public static final Type<ServerHandshakePacket> TYPE = new Type<>(JadeIds.PACKET_SERVER_HANDSHAKE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerHandshakePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, Identifier.STREAM_CODEC, JadeCodecs.PRIMITIVE_STREAM_CODEC),
			ServerHandshakePacket::serverConfig,
			ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()),
			ServerHandshakePacket::shearableBlocks,
			ByteBufCodecs.<ByteBuf, Identifier>list().apply(Identifier.STREAM_CODEC),
			ServerHandshakePacket::blockProviderIds,
			ByteBufCodecs.<ByteBuf, Identifier>list().apply(Identifier.STREAM_CODEC),
			ServerHandshakePacket::entityProviderIds,
			ServerHandshakePacket::new);

	public static void handle(ServerHandshakePacket message, ClientPayloadContext context) {
		context.execute(() -> {
			ObjectDataCenter.serverConnected = true;
			HarvestToolProvider.INSTANCE.setShearableBlocks(message.shearableBlocks);
			WailaClientRegistration.instance().setServerConfig(message.serverConfig);
			WailaCommonRegistration.instance().blockDataProviders.remapIds(message.blockProviderIds);
			WailaCommonRegistration.instance().entityDataProviders.remapIds(message.entityProviderIds);
			Jade.LOGGER.info("Received config from the server: {}", message.serverConfig);
		});
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
