package snownee.jade.impl.lookup;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;

import net.minecraft.core.IdMap;
import net.minecraft.core.IdMapper;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.PriorityStore;
import snownee.jade.impl.WailaCommonRegistration;

public interface IHierarchyLookup<T extends IJadeProvider> {
	Comparator<IJadeProvider> COMPARATOR = Comparator.comparingInt($ -> WailaCommonRegistration.instance().priorities.byValue($));

	default IHierarchyLookup<? extends T> cast() {
		return this;
	}

	void idMapped();

	@Nullable
	IdMapper<T> idMapper();

	default List<ResourceLocation> mappedIds() {
		return Streams.stream(Objects.requireNonNull(idMapper()))
				.map(IJadeProvider::getUid)
				.toList();
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

	void keyed();

	T byKey(ResourceLocation key);

	boolean isEmpty();

	Stream<Map.Entry<Class<?>, Collection<T>>> entries();

	void invalidate();

	void loadComplete(PriorityStore<ResourceLocation, IJadeProvider> priorityStore);

	default IdMapper<T> createIdMapper() {
		List<T> list = entries().flatMap(entry -> entry.getValue().stream()).toList();
		IdMapper<T> idMapper = idMapper();
		if (idMapper == null) {
			idMapper = new IdMapper<>(list.size());
		}
		for (T provider : list) {
			if (idMapper.getId(provider) == IdMap.DEFAULT) {
				idMapper.add(provider);
			}
		}
		return idMapper;
	}

	default void remapIds(List<ResourceLocation> keys) {
		IdMapper<T> idMapper = Objects.requireNonNull(idMapper());
		int i = 0;
		for (ResourceLocation key : keys) {
			T object = byKey(key);
			if (object != null) {
				idMapper.addMapping(object, i);
			}
			i++;
		}
	}
}
