package snownee.jade.impl;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.util.JsonConfig;

public class PriorityStore<T> {

	private final Object2IntMap<ResourceLocation> priorities = new Object2IntLinkedOpenHashMap<>();
	private final Function<T, ResourceLocation> uidGetter;
	private final ToIntFunction<T> defaultGetter;
	private final String fileName;
	private ImmutableList<ResourceLocation> sortedList = ImmutableList.of();

	public PriorityStore(String filename, ToIntFunction<T> defaultGetter, Function<T, ResourceLocation> uidGetter) {
		this.fileName = filename;
		this.defaultGetter = defaultGetter;
		this.uidGetter = uidGetter;
	}

	public void put(T provider) {
		Objects.requireNonNull(provider);
		ResourceLocation uid = uidGetter.apply(provider);
		Objects.requireNonNull(uid);
		priorities.put(uid, defaultGetter.applyAsInt(provider));
	}

	public void updateConfig(Set<ResourceLocation> allKeys) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<LinkedHashMap<ResourceLocation, Integer>>() {
		}.getType();
		JsonConfig<Map<ResourceLocation, Integer>> config = new JsonConfig<>(fileName, type, null, LinkedHashMap::new);
		Map<ResourceLocation, Integer> map = config.get();
		for (var e : map.entrySet()) {
			if (e.getValue() != null)
				priorities.put(e.getKey(), e.getValue().intValue());
		}
		new Thread(() -> {
			for (ResourceLocation id : priorities.keySet()) {
				if (!map.containsKey(id)) {
					map.put(id, null);
				}
			}
			config.save();
		}).start();

		List<ResourceLocation> keys = allKeys.stream().filter($ -> !$.getPath().contains(".")).sorted(Comparator.comparingInt(this::get)).collect(Collectors.toCollection(LinkedList::new));
		allKeys.stream().filter($ -> $.getPath().contains(".")).forEach($ -> {
			ResourceLocation parent = new ResourceLocation($.getNamespace(), $.getPath().substring(0, $.getPath().indexOf('.')));
			int index = keys.indexOf(parent);
			keys.add(index + 1, $);
		});
		sortedList = ImmutableList.copyOf(keys);
	}

	public int get(T value) {
		return get(uidGetter.apply(value));
	}

	public int get(ResourceLocation id) {
		return priorities.getInt(id);
	}

	public ImmutableList<ResourceLocation> getSortedList() {
		return sortedList;
	}
}
