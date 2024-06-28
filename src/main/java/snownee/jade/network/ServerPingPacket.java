package snownee.jade.network;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import snownee.jade.Jade;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.JsonConfig;

public record ServerPingPacket(String serverConfig, List<Block> shearableBlocks) implements CustomPacketPayload {
	public static final Type<ServerPingPacket> TYPE = new Type<>(JadeIds.PACKET_SERVER_PING);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerPingPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			ServerPingPacket::serverConfig,
			ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()),
			ServerPingPacket::shearableBlocks,
			ServerPingPacket::new
	);

	public static void handle(ServerPingPacket message, ClientPayloadContext context) {
		String s = message.serverConfig;
		JsonObject json;
		try {
			json = s.isEmpty() ? null : JsonConfig.GSON.fromJson(s, JsonObject.class);
		} catch (Throwable e) {
			Jade.LOGGER.error("Received malformed config from the server: {}", s);
			return;
		}
		context.execute(() -> {
			ObjectDataCenter.serverConnected = true;
			PluginConfig.INSTANCE.reload(); // clear the server config last time we applied
			if (json != null && !json.keySet().isEmpty()) {
				PluginConfig.INSTANCE.applyServerConfigs(json);
			}
			HarvestToolProvider.INSTANCE.setShearableBlocks(message.shearableBlocks);
			Jade.LOGGER.info("Received config from the server: {}", s);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
