package snownee.jade.network;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import snownee.jade.Jade;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.JsonConfig;

public class ServerPingPacket {

	public final String serverConfig;

	public ServerPingPacket(String serverConfig) {
		this.serverConfig = Strings.nullToEmpty(serverConfig);
	}

	public ServerPingPacket(PluginConfig config) {
		this(PluginConfig.INSTANCE.getServerConfigs());
	}

	public static ServerPingPacket read(FriendlyByteBuf buffer) {
		return new ServerPingPacket(buffer.readUtf());
	}

	public static void write(ServerPingPacket message, FriendlyByteBuf buffer) {
		buffer.writeUtf(message.serverConfig);
	}

	public static void handle(ServerPingPacket message, CustomPayloadEvent.Context context) {
		String s = message.serverConfig;
		JsonObject json;
		try {
			json = s.isEmpty() ? null : JsonConfig.DEFAULT_GSON.fromJson(s, JsonObject.class);
		} catch (Throwable e) {
			Jade.LOGGER.error("Received malformed config from the server: {}", s);
			return;
		}
		context.enqueueWork(() -> {
			ObjectDataCenter.serverConnected = true;
			PluginConfig.INSTANCE.reload(); // clear the server config last time we applied
			if (json != null && !json.keySet().isEmpty())
				PluginConfig.INSTANCE.applyServerConfigs(json);
			Jade.LOGGER.info("Received config from the server: {}", s);
		});
		context.setPacketHandled(true);
	}
}
