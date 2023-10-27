package snownee.jade.api.view;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.Accessor;
import snownee.jade.util.CommonProxy;

public class ItemView {

	public ItemStack item;
	@Nullable
	public String text;

	public ItemView(ItemStack item) {
		this(item, null);
	}

	public ItemView(ItemStack item, @Nullable String text) {
		this.item = item;
		this.text = text;
	}

	public static List<ViewGroup<ItemStack>> groupOf(Container container, Accessor<?> accessor) {
		return CommonProxy.containerGroup(container, accessor);
	}

	/**
	 * @param storage  On Fabric, it accepts {@code Storage<ItemVariant>}. On Forge, it accepts {@code IItemHandler}.
	 * @param accessor
	 */
	public static List<ViewGroup<ItemStack>> groupOf(Object storage, Accessor<?> accessor) {
		return CommonProxy.storageGroup(storage, accessor);
	}

}
