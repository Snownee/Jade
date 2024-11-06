package snownee.jade.network;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
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
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.JadeCodecs;

public record ServerConfigurePacket(
		Map<ResourceLocation, Object> serverConfig,
		List<Block> shearableBlocks,
		List<ResourceLocation> blockProviderIds,
		List<ResourceLocation> entityProviderIds) implements CustomPacketPayload {
	public static final Type<ServerConfigurePacket> TYPE = new Type<>(JadeIds.PACKET_SERVER_CONFIGURE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigurePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ResourceLocation.STREAM_CODEC, JadeCodecs.PRIMITIVE_STREAM_CODEC),
			ServerConfigurePacket::serverConfig,
			ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()),
			ServerConfigurePacket::shearableBlocks,
			ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC),
			ServerConfigurePacket::blockProviderIds,
			ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC),
			ServerConfigurePacket::entityProviderIds,
			ServerConfigurePacket::new);

	public static void handle(ServerConfigurePacket message, ClientPayloadContext context) {
		context.execute(() -> {
			HarvestToolProvider.INSTANCE.setShearableBlocks(message.shearableBlocks);
			WailaClientRegistration.instance().setServerConfig(message.serverConfig);
			WailaCommonRegistration.instance().blockDataProviders.remapIds(message.blockProviderIds);
			WailaCommonRegistration.instance().entityDataProviders.remapIds(message.entityProviderIds);
			Jade.LOGGER.info("Received config from the server: {}", message.serverConfig);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
