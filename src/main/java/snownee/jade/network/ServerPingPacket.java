package snownee.jade.network;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.Jade;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.util.JadeCodecs;

public record ServerPingPacket(Map<ResourceLocation, Object> serverConfig, List<Block> shearableBlocks) implements CustomPacketPayload {
	public static final Type<ServerPingPacket> TYPE = new Type<>(JadeIds.PACKET_SERVER_PING);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerPingPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ResourceLocation.STREAM_CODEC, JadeCodecs.PRIMITIVE_STREAM_CODEC),
			ServerPingPacket::serverConfig,
			ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()),
			ServerPingPacket::shearableBlocks,
			ServerPingPacket::new);

	public static void handle(ServerPingPacket message, ClientPayloadContext context) {
		context.execute(() -> {
			ObjectDataCenter.serverConnected = true;
			HarvestToolProvider.INSTANCE.setShearableBlocks(message.shearableBlocks);
			WailaClientRegistration.instance().setServerConfig(message.serverConfig);
			Jade.LOGGER.info("Received config from the server: {}", message.serverConfig);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
