package snownee.jade.impl;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public class PriorityStore<T> {

	private final Object2IntMap<ResourceLocation> priorities = new Object2IntOpenHashMap<>();
	private final Function<T, ResourceLocation> uidGetter;
	private final ToIntFunction<T> defaultGetter;

	public PriorityStore(String filename, ToIntFunction<T> defaultGetter, Function<T, ResourceLocation> uidGetter) {
		this.defaultGetter = defaultGetter;
		this.uidGetter = uidGetter;
	}

	public void put(T provider) {
		Objects.requireNonNull(provider);
		ResourceLocation uid = uidGetter.apply(provider);
		Objects.requireNonNull(uid);
		priorities.put(uid, defaultGetter.applyAsInt(provider));
	}

	public void updateConfig() {
		//TODO
	}

	public int get(T value) {
		return get(uidGetter.apply(value));
	}

	public int get(ResourceLocation id) {
		return priorities.getInt(id);
	}

}
