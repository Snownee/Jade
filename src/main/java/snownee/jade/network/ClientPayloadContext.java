package snownee.jade.network;

import net.minecraft.client.Minecraft;
import snownee.jade.util.ClientProxy;

public interface ClientPayloadContext {
	static ClientPayloadContext of(Minecraft client) {
		return client.player != null ? runnable -> client.execute(() -> ClientProxy.runWithContext(client, runnable)) : client::execute;
	}

	void execute(Runnable runnable);
}
