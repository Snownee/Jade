package snownee.jade.api.view;

import java.util.List;
import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.Accessor;
import snownee.jade.util.CommonProxy;

public interface ItemViewUtils {
	static List<ViewGroup<ItemStack>> groupOf(Container container, Accessor<?> accessor) {
		return CommonProxy.containerGroup(container, accessor);
	}

	static List<ViewGroup<ItemStack>> groupOf(Container container, Accessor<?> accessor, Function<Accessor<?>, Container> containerFinder) {
		return CommonProxy.containerGroup(container, accessor, containerFinder);
	}

	/**
	 * @param storage On Fabric, it accepts {@code Storage<ItemVariant>}. On NeoForge, it accepts {@code IItemHandler}.
	 */
	static List<ViewGroup<ItemStack>> groupOf(Object storage, Accessor<?> accessor) {
		return CommonProxy.storageGroup(storage, accessor);
	}

	/**
	 * @param storage On Fabric, it accepts {@code Storage<ItemVariant>}. On NeoForge, it accepts {@code IItemHandler}.
	 */
	static List<ViewGroup<ItemStack>> groupOf(Object storage, Accessor<?> accessor, Function<Accessor<?>, Object> storageFinder) {
		return CommonProxy.storageGroup(storage, accessor, storageFinder);
	}
}
