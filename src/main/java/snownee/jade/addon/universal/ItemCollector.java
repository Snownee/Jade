package snownee.jade.addon.universal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ViewGroup;

public class ItemCollector<T> {
	public static final int MAX_SIZE = 54;
	public static final ItemCollector<?> EMPTY = new ItemCollector<>(null);
	private static final CompoundTag IGNORED_TAG = new CompoundTag();

	static {
		IGNORED_TAG.putBoolean("__JadeClear", true);
	}

	private static final Predicate<ItemStack> SHOWN = stack -> {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).hideTooltip()) {
			return false;
		}
		if (stack.hasNonDefault(DataComponents.CUSTOM_MODEL_DATA) || stack.hasNonDefault(DataComponents.ITEM_MODEL)) {
			CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
			if (customData.matchedBy(IGNORED_TAG)) {
				return false;
			}
		}
		return true;
	};
	private final Items items = new Items();
	private final ItemIterator<T> iterator;
	public long version;
	public long lastTimeFinished;
	public boolean lastTimeIsEmpty;
	public @Nullable List<ViewGroup<ItemStack>> mergedResult;
	public @Nullable List<ViewGroup<ItemStack>> sortedMergedResult;

	public ItemCollector(ItemIterator<T> iterator) {
		this.iterator = iterator;
	}

	public List<ViewGroup<ItemStack>> update(Accessor<?> accessor) {
		if (iterator == null) {
			return null;
		}
		T container = iterator.find(accessor);
		if (container == null) {
			return null;
		}
		boolean sorted = accessor.getServerData().getBooleanOr("SortItems", false);
		long currentVersion = iterator.getVersion(container);
		long gameTime = accessor.getLevel().getGameTime();
		List<ViewGroup<ItemStack>> result = sorted ? sortedMergedResult : mergedResult;
		if (result != null && iterator.isFinished()) {
			if (version == currentVersion) {
				return result; // content not changed
			}
			if (lastTimeFinished + 5 > gameTime) {
				return result; // avoid update too frequently
			}
			iterator.reset();
		}
		AtomicInteger count = new AtomicInteger();
		iterator.populate(container, MAX_SIZE * 2).forEach(stack -> {
			count.incrementAndGet();
			if (SHOWN.test(stack)) {
				ItemDefinition def = new ItemDefinition(stack);
				items.addTo(def, stack.getCount());
			}
		});
		iterator.afterPopulate(count.get());
		if (result != null && !iterator.isFinished()) {
			updateCollectingProgress(result.getFirst());
			return result;
		}
		List<ItemStack> partialResult = items.partialResult(sorted);
		List<ViewGroup<ItemStack>> groups = List.of(updateCollectingProgress(new ViewGroup<>(partialResult)));
		if (iterator.isFinished()) {
			if (sorted) {
				mergedResult = List.of(updateCollectingProgress(new ViewGroup<>(items.partialResult(false))));
				sortedMergedResult = groups;
			} else {
				mergedResult = groups;
				sortedMergedResult = List.of(updateCollectingProgress(new ViewGroup<>(items.partialResult(true))));
			}
			lastTimeIsEmpty = groups.getFirst().views.isEmpty();
			version = currentVersion;
			lastTimeFinished = gameTime;
			items.clear();
		}
		return groups;
	}

	protected ViewGroup<ItemStack> updateCollectingProgress(ViewGroup<ItemStack> group) {
		if (lastTimeIsEmpty && group.views.isEmpty()) {
			return group;
		}
		float progress = iterator.getCollectingProgress();
		CompoundTag data = group.getExtraData();
		if (Float.isNaN(progress) || progress >= 1) {
			data.remove("Collecting");
		} else {
			data.putFloat("Collecting", progress);
		}
		return group;
	}

	public record ItemDefinition(Item item, DataComponentPatch components) {
		ItemDefinition(ItemStack stack) {
			this(stack.getItem(), stack.getComponentsPatch());
		}

		public ItemStack toStack(int count) {
			ItemStack itemStack = new ItemStack(item, count);
			itemStack.applyComponents(components);
			return itemStack;
		}
	}

	private static class Items {
		private final Object2IntLinkedOpenHashMap<ItemDefinition> items = new Object2IntLinkedOpenHashMap<>();
		private final List<ItemDefinition> sorted = Lists.newArrayList();
		private final Set<ItemDefinition> sortedSet = Sets.newLinkedHashSet();
		private int smallestCount;

		public void clear() {
			smallestCount = 0;
			items.clear();
			sorted.clear();
			sortedSet.clear();
		}

		public List<ItemStack> partialResult(boolean sort) {
			if (sort) {
				sorted.sort((a, b) -> -Integer.compare(items.getInt(a), items.getInt(b)));
				if (sorted.size() > MAX_SIZE) {
					sorted.subList(MAX_SIZE, sorted.size()).clear();
					sortedSet.clear();
					sortedSet.addAll(sorted);
				}
				if (!sorted.isEmpty()) {
					smallestCount = items.getInt(sorted.getLast());
				}
				return sorted.stream()
						.map(def -> def.toStack(items.getInt(def)))
						.toList();
			} else {
				return items.object2IntEntrySet().stream()
						.limit(MAX_SIZE)
						.map(entry -> entry.getKey().toStack(entry.getIntValue()))
						.toList();
			}
		}

		public void addTo(ItemDefinition def, int count) {
			int old = items.addTo(def, count);
			if (!sortedSet.contains(def)) {
				count += old;
				if (sorted.size() < MAX_SIZE) {
					sortedSet.add(def);
					sorted.add(def);
					smallestCount = Math.min(smallestCount, count);
				} else if (count > smallestCount) {
					sortedSet.add(def);
					sorted.add(def);
				}
			}
		}
	}
}
