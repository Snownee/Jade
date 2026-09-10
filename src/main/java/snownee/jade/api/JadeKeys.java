package snownee.jade.api;

import java.util.Objects;

import net.minecraft.client.KeyMapping;
import snownee.jade.JadeClient;

/**
 * Accessors for Jade's client key mappings.
 */
public interface JadeKeys {

	/**
	 * Returns the config screen key binding.
	 *
	 * @return the config key mapping
	 */
	static KeyMapping openConfig() {
		return Objects.requireNonNull(JadeClient.openConfig);
	}

	/**
	 * Returns the overlay toggle key binding.
	 *
	 * @return the overlay key mapping
	 */
	static KeyMapping showOverlay() {
		return Objects.requireNonNull(JadeClient.showOverlay);
	}

	/**
	 * Returns the liquid toggle key binding.
	 *
	 * @return the liquid key mapping
	 */
	static KeyMapping toggleLiquid() {
		return Objects.requireNonNull(JadeClient.toggleLiquid);
	}

	/**
	 * Returns the details modifier key binding.
	 *
	 * @return the details key mapping
	 */
	static KeyMapping showDetails() {
		return Objects.requireNonNull(JadeClient.showDetails);
	}

	/**
	 * Returns the narration key binding.
	 *
	 * @return the narration key mapping
	 */
	static KeyMapping narrate() {
		return Objects.requireNonNull(JadeClient.narrate);
	}

	/**
	 * Returns the recipe key binding.
	 *
	 * @return the recipes key mapping
	 */
	static KeyMapping showRecipes() {
		return Objects.requireNonNull(JadeClient.showRecipes);
	}

	/**
	 * Returns the uses key binding.
	 *
	 * @return the uses key mapping
	 */
	static KeyMapping showUses() {
		return Objects.requireNonNull(JadeClient.showUses);
	}

	/**
	 * Returns the profile key binding at the given index.
	 *
	 * @param index profile slot index
	 * @return the profile key mapping
	 */
	static KeyMapping useProfile(int index) {
		return Objects.requireNonNull(JadeClient.profiles[index]);
	}
}
