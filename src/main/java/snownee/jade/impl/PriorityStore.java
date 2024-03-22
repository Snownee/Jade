package snownee.jade.impl;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import snownee.jade.util.JsonConfig;

public class PriorityStore<K, V> {

	private final Object2IntMap<K> priorities = new Object2IntLinkedOpenHashMap<>();
	private final Function<V, K> keyGetter;
	private final ToIntFunction<V> defaultPriorityGetter;
	@Nullable
	private String configFile;
	private ImmutableList<K> sortedList = ImmutableList.of();
	private BiFunction<PriorityStore<K, V>, Collection<K>, List<K>> sortingFunction = (store, allKeys) -> allKeys.stream()
			.sorted(Comparator.comparingInt(store::byKey))
			.toList();

	public PriorityStore(ToIntFunction<V> defaultPriorityGetter, Function<V, K> keyGetter) {
		this.defaultPriorityGetter = defaultPriorityGetter;
		this.keyGetter = keyGetter;
	}

	public void setSortingFunction(BiFunction<PriorityStore<K, V>, Collection<K>, List<K>> sortingFunction) {
		this.sortingFunction = sortingFunction;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public void put(V provider) {
		Objects.requireNonNull(provider);
		put(provider, defaultPriorityGetter.applyAsInt(provider));
	}

	public void put(V provider, int priority) {
		Objects.requireNonNull(provider);
		K uid = keyGetter.apply(provider);
		Objects.requireNonNull(uid);
		priorities.put(uid, priority);
	}

	public void sort(Set<K> extraKeys) {
		Set<K> allKeys = priorities.keySet();
		if (!extraKeys.isEmpty()) {
			allKeys = Sets.union(priorities.keySet(), extraKeys);
		}

		if (!Strings.isNullOrEmpty(configFile)) {
			@SuppressWarnings("serial")
			Type type = new TypeToken<LinkedHashMap<K, Integer>>() {
			}.getType();
			JsonConfig<Map<K, Integer>> config = new JsonConfig<>(configFile, type, null, LinkedHashMap::new);
			Map<K, Integer> map = config.get();
			for (var e : map.entrySet()) {
				if (e.getValue() != null) {
					priorities.put(e.getKey(), e.getValue().intValue());
				}
			}
			new Thread(() -> {
				for (K id : priorities.keySet()) {
					if (!map.containsKey(id)) {
						map.put(id, null);
					}
				}
				config.save();
			}).start();
		}

		sortedList = ImmutableList.copyOf(sortingFunction.apply(this, allKeys));
	}

	public int byValue(V value) {
		return byKey(keyGetter.apply(value));
	}

	public int byKey(K id) {
		return priorities.getInt(id);
	}

	public ImmutableList<K> getSortedList() {
		return sortedList;
	}
}
