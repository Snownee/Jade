package snownee.jade.network;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import snownee.jade.Jade;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.JsonConfig;

public record ServerPingPacket(String serverConfig) implements CustomPacketPayload {

	public static ServerPingPacket read(FriendlyByteBuf buffer) {
		return new ServerPingPacket(buffer.readUtf());
	}

	public static void handle(ServerPingPacket message, PlayPayloadContext context) {
		String s = message.serverConfig;
		JsonObject json;
		try {
			json = s.isEmpty() ? null : JsonConfig.DEFAULT_GSON.fromJson(s, JsonObject.class);
		} catch (Throwable e) {
			Jade.LOGGER.error("Received malformed config from the server: {}", s);
			return;
		}
		context.workHandler().execute(() -> {
			ObjectDataCenter.serverConnected = true;
			PluginConfig.INSTANCE.reload(); // clear the server config last time we applied
			if (json != null && !json.keySet().isEmpty())
				PluginConfig.INSTANCE.applyServerConfigs(json);
			Jade.LOGGER.info("Received config from the server: {}", s);
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(serverConfig);
	}

	@Override
	public ResourceLocation id() {
		return Identifiers.PACKET_SERVER_PING;
	}
}
