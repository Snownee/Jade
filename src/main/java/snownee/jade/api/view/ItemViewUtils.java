package snownee.jade.api.view;

import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.Accessor;
import snownee.jade.util.CommonProxy;

/**
 * Helper methods for building item storage view groups.
 */
public interface ItemViewUtils {
	/**
	 * Groups items from a {@link Container}.
	 *
	 * @param container item container
	 * @param accessor current accessor
	 * @return grouped views, or {@code null}
	 */
	@Nullable
	static List<ViewGroup<ItemStack>> groupOf(Container container, Accessor<?> accessor) {
		return CommonProxy.containerGroup(container, accessor);
	}

	/**
	 * Groups items from a container resolved from the accessor.
	 *
	 * @param container item container
	 * @param accessor current accessor
	 * @param containerFinder container lookup callback
	 * @return grouped views, or {@code null}
	 */
	@Nullable
	static List<ViewGroup<ItemStack>> groupOf(Container container, Accessor<?> accessor, Function<Accessor<?>, Container> containerFinder) {
		return CommonProxy.containerGroup(container, accessor, containerFinder);
	}

	/**
	 * Groups items from a platform-specific storage object.
	 *
	 * @param storage on Fabric, {@code Storage<ItemVariant>}; on NeoForge, {@code IItemHandler}
	 * @param accessor current accessor
	 * @return grouped views, or {@code null}
	 */
	@Nullable
	static List<ViewGroup<ItemStack>> groupOf(Object storage, Accessor<?> accessor) {
		return CommonProxy.storageGroup(storage, accessor);
	}

	/**
	 * Groups items from a resolved platform-specific storage object.
	 *
	 * @param storage on Fabric, {@code Storage<ItemVariant>}; on NeoForge, {@code IItemHandler}
	 * @param accessor current accessor
	 * @param storageFinder storage lookup callback
	 * @return grouped views, or {@code null}
	 */
	@Nullable
	static List<ViewGroup<ItemStack>> groupOf(Object storage, Accessor<?> accessor, Function<Accessor<?>, Object> storageFinder) {
		return CommonProxy.storageGroup(storage, accessor, storageFinder);
	}
}
