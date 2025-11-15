package snownee.jade.impl.lookup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import net.minecraft.core.IdMapper;
import net.minecraft.resources.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.PriorityStore;

public class PairHierarchyLookup<T extends IJadeProvider> implements IHierarchyLookup<T> {
	public final IHierarchyLookup<T> first;
	public final IHierarchyLookup<T> second;
	private final Cache<Pair<Class<?>, Class<?>>, List<T>> mergedCache = CacheBuilder.newBuilder().build();
	protected boolean idMapped;
	@Nullable
	protected IdMapper<T> idMapper;
	protected Map<Identifier, T> byKey;

	public PairHierarchyLookup(IHierarchyLookup<T> first, IHierarchyLookup<T> second) {
		this.first = first;
		this.second = second;
	}

	@SuppressWarnings("unchecked")
	public <ANY> List<ANY> getMerged(Object first, Object second) {
		Objects.requireNonNull(first);
		Objects.requireNonNull(second);
		try {
			return (List<ANY>) mergedCache.get(
					Pair.of(first.getClass(), second.getClass()), () -> {
						List<T> firstList = this.first.get(first);
						List<T> secondList = this.second.get(second);
						if (firstList.isEmpty()) {
							return secondList;
						} else if (secondList.isEmpty()) {
							return firstList;
						}
						return ImmutableList.sortedCopyOf(COMPARATOR, Iterables.concat(firstList, secondList));
					});
		} catch (ExecutionException e) {
			Jade.LOGGER.error("", e);
		}
		return List.of();
	}

	@Override
	public void idMapped() {
		idMapped = true;
		keyed();
	}

	@Override
	public @Nullable IdMapper<T> idMapper() {
		return idMapper;
	}

	@Override
	public void register(Class<?> clazz, T provider) {
		if (first.isClassAcceptable(clazz)) {
			first.register(clazz, provider);
		} else if (second.isClassAcceptable(clazz)) {
			second.register(clazz, provider);
		} else {
			throw new IllegalArgumentException("Class " + clazz + " is not acceptable");
		}
		if (byKey != null) {
			T oldProvider = byKey.put(provider.getUid(), provider);
			if (oldProvider != provider && oldProvider != null) {
				Jade.LOGGER.warn(
						"Found different provider instances with same id {}, this may cause issues: {} and {}",
						provider.getUid(),
						oldProvider,
						provider);
			}
		}
	}

	@Override
	public boolean isClassAcceptable(Class<?> clazz) {
		return first.isClassAcceptable(clazz) || second.isClassAcceptable(clazz);
	}

	@Override
	public List<T> get(Class<?> clazz) {
		List<T> result = first.get(clazz);
		if (result.isEmpty()) {
			result = second.get(clazz);
		}
		return result;
	}

	@Override
	public boolean isEmpty() {
		return first.isEmpty() && second.isEmpty();
	}

	@Override
	public Stream<Map.Entry<Class<?>, Collection<T>>> entries() {
		return Stream.concat(first.entries(), second.entries());
	}

	@Override
	public void invalidate() {
		first.invalidate();
		second.invalidate();
		mergedCache.invalidateAll();
	}

	@Override
	public void loadComplete(PriorityStore<Identifier, IJadeProvider> priorityStore) {
		first.loadComplete(priorityStore);
		second.loadComplete(priorityStore);
		if (idMapped) {
			idMapper = createIdMapper();
		}
	}

	@Override
	public void keyed() {
		byKey = Maps.newHashMap();
	}

	@Override
	public T byKey(Identifier key) {
		return byKey.get(key);
	}
}
