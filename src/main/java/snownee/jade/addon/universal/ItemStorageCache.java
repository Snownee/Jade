package snownee.jade.addon.universal;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.view.ViewGroup;

public class ItemStorageCache<T> {
	public static final int MAX_SIZE = 54;
	public static final ItemStorageCache<?> EMPTY = new ItemStorageCache<>(null);
	private static final Predicate<ItemStack> NON_EMPTY = stack -> {
		if (stack.isEmpty()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("CustomModelData")) {
			for (String key : stack.getTag().getAllKeys()) {
				if (key.toLowerCase(Locale.ENGLISH).endsWith("clear") && stack.getTag().getBoolean(key)) {
					return false;
				}
			}
		}
		return true;
	};
	private final Object2IntLinkedOpenHashMap<ItemDefinition> items = new Object2IntLinkedOpenHashMap<>();
	private final ItemIterator<T> iterator;
	public long version;
	public long lastTimeFinished;
	public List<ViewGroup<ItemStack>> mergedResult;

	public ItemStorageCache(ItemIterator<T> iterator) {
		this.iterator = iterator;
	}

	public List<ViewGroup<ItemStack>> update(Object target, long gameTime) {
		if (iterator == null) {
			return null;
		}
		T container = iterator.find(target);
		if (container == null) {
			return null;
		}
		long currentVersion = iterator.getVersion(container);
		if (mergedResult != null && iterator.isFinished()) {
			if (version == currentVersion) {
				return mergedResult; // content not changed
			}
			if (gameTime == lastTimeFinished) {
				return mergedResult; // only update once per tick
			}
			iterator.reset();
		}
		iterator.populate(container).filter(NON_EMPTY).forEach(stack -> {
			ItemDefinition def = new ItemDefinition(stack);
			items.addTo(def, stack.getCount());
		});
		if (mergedResult != null && !iterator.isFinished()) {
			return mergedResult;
		}
		List<ItemStack> partialResult = items.object2IntEntrySet().stream().limit(54).map(entry -> {
			ItemDefinition def = entry.getKey();
			int count = entry.getIntValue();
			return def.toStack(count);
		}).toList();
		List<ViewGroup<ItemStack>> groups = List.of(new ViewGroup<>(partialResult));
		if (iterator.isFinished()) {
			mergedResult = groups;
			version = currentVersion;
			lastTimeFinished = gameTime;
			items.clear();
		}
		return groups;
	}

	public record ItemDefinition(Item item, @Nullable CompoundTag tag) {
		ItemDefinition(ItemStack stack) {
			this(stack.getItem(), stack.getTag());
		}

		public ItemStack toStack(int count) {
			ItemStack stack = new ItemStack(item);
			stack.setCount(count);
			stack.setTag(tag);
			return stack;
		}
	}
}
