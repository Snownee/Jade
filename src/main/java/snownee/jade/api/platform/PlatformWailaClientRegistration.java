package snownee.jade.api.platform;

import net.minecraft.world.level.block.Block;

/**
 * Platform-specific client registration hooks.
 */
public interface PlatformWailaClientRegistration {

	/**
	 * Registers a custom enchantment power provider for the given block.
	 *
	 * @param block block to extend
	 * @param customEnchantPower bonus calculator
	 */
	void registerCustomEnchantPower(Block block, CustomEnchantPower customEnchantPower);

}
