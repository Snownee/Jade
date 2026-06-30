package snownee.jade.api.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Calculates an enchantment power bonus for a block.
 */
@FunctionalInterface
public interface CustomEnchantPower {

	/**
	 * Returns the enchantment power bonus for a block state.
	 *
	 * @param state target block state
	 * @param world current level
	 * @param pos block position
	 * @return enchantment power bonus
	 */
	float getEnchantPowerBonus(BlockState state, Level world, BlockPos pos);

}
