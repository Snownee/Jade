package snownee.jade.network;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.client.Minecraft;

public interface ClientPayloadContext {
	static ClientPayloadContext of(Minecraft client) {
		return client.player != null ? runnable -> client.execute(() -> PacketContext.runWithContext(client.player, runnable)) : client::execute;
	}

	void execute(Runnable runnable);
}
