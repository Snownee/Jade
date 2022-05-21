package snownee.jade.impl;

import java.util.Map;
import java.util.function.ToIntFunction;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public class PriorityStore<T> {

	private final Map<ResourceLocation, T> dedupe = Maps.newHashMap();
	private final Object2IntMap<T> priorities = new Object2IntOpenHashMap<>();
	private final ToIntFunction<T> defaultGetter;

	public PriorityStore(String filename, ToIntFunction<T> defaultGetter) {
		this.defaultGetter = defaultGetter;
	}

	public void put(ResourceLocation id, T value) {
		if (dedupe.containsKey(id) || dedupe.containsValue(value)) {
			if (dedupe.get(id) != value) {
				throw new IllegalStateException("Duplicate item");
			}
			return;
		}
		dedupe.put(id, value);
		priorities.put(value, defaultGetter.applyAsInt(value));
	}

	public void updateConfig() {
		//TODO
	}

	public int get(T value) {
		return priorities.getInt(value);
	}

	public int get(ResourceLocation id) {
		return get(dedupe.get(id));
	}

}
