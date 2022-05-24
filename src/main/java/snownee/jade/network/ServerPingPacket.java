package snownee.jade.network;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import snownee.jade.Jade;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.ConfigEntry;
import snownee.jade.impl.config.PluginConfig;

public class ServerPingPacket {

	public Map<ResourceLocation, Boolean> forcedKeys = Maps.newHashMap();

	public ServerPingPacket(@Nullable Map<ResourceLocation, Boolean> forcedKeys) {
		this.forcedKeys = forcedKeys;
	}

	public ServerPingPacket(PluginConfig config) {
		Set<ConfigEntry> entries = config.getSyncableConfigs();
		entries.forEach(e -> forcedKeys.put(e.getId(), e.getValue()));
	}

	public static ServerPingPacket read(FriendlyByteBuf buffer) {
		int size = buffer.readVarInt();
		Map<ResourceLocation, Boolean> temp = Maps.newHashMap();
		for (int i = 0; i < size; i++) {
			ResourceLocation id = new ResourceLocation(buffer.readUtf(128));
			boolean value = buffer.readBoolean();
			temp.put(id, value);
		}

		return new ServerPingPacket(temp);
	}

	public static void write(ServerPingPacket message, FriendlyByteBuf buffer) {
		buffer.writeVarInt(message.forcedKeys.size());
		message.forcedKeys.forEach((k, v) -> {
			buffer.writeUtf(k.toString());
			buffer.writeBoolean(v);
		});
	}

	public static class Handler {

		public static void onMessage(ServerPingPacket message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				ObjectDataCenter.serverConnected = true;
				message.forcedKeys.forEach(PluginConfig.INSTANCE::set);
				Jade.LOGGER.info("Received config from the server: {}", new Gson().toJson(message.forcedKeys));
			});
			context.get().setPacketHandled(true);
		}
	}
}
