package snownee.jade.api.view;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemView {

	public ItemStack item;
	@Nullable
	public String text;

	public ItemView(ItemStack item, @Nullable String text) {
		this.item = item;
		this.text = text;
	}

	public static List<ItemView> fromContainer(Container container, int maxSize, int startIndex) {
		return compactViews(IntStream.range(startIndex, container.getContainerSize()).limit(maxSize * 3).mapToObj(container::getItem), maxSize);
	}

	public static List<ItemView> fromItemHandler(IItemHandler itemHandler, int maxSize, int startIndex) {
		return compactViews(IntStream.range(startIndex, itemHandler.getSlots()).limit(maxSize * 3).mapToObj(itemHandler::getStackInSlot), maxSize);
	}

	public static List<ItemView> compactViews(Stream<ItemStack> stream, int maxSize) {
		List<ItemView> views = Lists.newArrayList();
		MutableInt start = new MutableInt();
		/* off */
		stream
				.filter(stack -> !stack.isEmpty())
				.filter(stack -> {
					if (stack.hasTag() && stack.getTag().contains("CustomModelData")) {
						for (String key : stack.getTag().getAllKeys()) {
							if (key.toLowerCase(Locale.ENGLISH).endsWith("clear") && stack.getTag().getBoolean(key)) {
								return false;
							}
						}
					}
					return true;
				})
				.forEach(stack -> {
					int size = views.size();
					if (size > maxSize)
						return;
					for (int i = 0; i < size; i++) {
						int j = (i + start.intValue()) % size;
						if (ItemStack.isSameItemSameTags(stack, views.get(j).item)) {
							views.get(j).item.grow(stack.getCount());
							start.setValue(j);
							return;
						}
					}
					views.add(new ItemView(stack.copy(), null));
				});
		/* on */
		if (views.size() > maxSize)
			views.remove(maxSize);
		return views;
	}

}
