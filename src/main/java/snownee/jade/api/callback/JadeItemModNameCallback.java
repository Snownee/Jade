package snownee.jade.api.callback;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

/**
 * Allows addons to override the mod name shown for an item.
 */
@FunctionalInterface
public interface JadeItemModNameCallback {

	/**
	 * Returns a mod name override for an item stack.
	 *
	 * @param stack item stack being rendered
	 * @return override mod name, or {@code null} to keep the default
	 */
	@Nullable
	String gatherItemModName(ItemStack stack);

}
