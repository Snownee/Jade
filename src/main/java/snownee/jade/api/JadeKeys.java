package snownee.jade.api;

import java.util.Objects;

import net.minecraft.client.KeyMapping;
import snownee.jade.JadeClient;
import snownee.jade.util.ClientProxy;

public interface JadeKeys {
	static boolean hasRecipeViewerKeys() {
		return ClientProxy.shouldRegisterRecipeViewerKeys();
	}

	static KeyMapping openConfig() {
		return Objects.requireNonNull(JadeClient.openConfig);
	}

	static KeyMapping showOverlay() {
		return Objects.requireNonNull(JadeClient.showOverlay);
	}

	static KeyMapping toggleLiquid() {
		return Objects.requireNonNull(JadeClient.toggleLiquid);
	}

	static KeyMapping showDetails() {
		return Objects.requireNonNull(JadeClient.showDetails);
	}

	static KeyMapping narrate() {
		return Objects.requireNonNull(JadeClient.narrate);
	}

	static KeyMapping showRecipes() {
		return Objects.requireNonNull(JadeClient.showRecipes);
	}

	static KeyMapping showUses() {
		return Objects.requireNonNull(JadeClient.showUses);
	}

	static KeyMapping useProfile(int index) {
		return Objects.requireNonNull(JadeClient.profiles[index]);
	}
}
