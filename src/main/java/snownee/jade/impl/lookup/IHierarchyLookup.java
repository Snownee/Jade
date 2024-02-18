package snownee.jade.impl.lookup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.PriorityStore;

public interface IHierarchyLookup<T extends IJadeProvider> {
	default IHierarchyLookup<? extends T> cast() {
		return this;
	}

	void register(Class<?> clazz, T provider);

	boolean isClassAcceptable(Class<?> clazz);

	default List<T> get(Object obj) {
		if (obj == null) {
			return List.of();
		}
		return get(obj.getClass());
	}

	List<T> get(Class<?> clazz);

	boolean isEmpty();

	Stream<Map.Entry<Class<?>, Collection<T>>> entries();

	void invalidate();

	void loadComplete(PriorityStore<ResourceLocation, IJadeProvider> priorityStore);
}
