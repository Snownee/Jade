package snownee.jade.api.callback;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface JadeItemModNameCallback {

	@Nullable
	String gatherItemModName(ItemStack stack);

}
