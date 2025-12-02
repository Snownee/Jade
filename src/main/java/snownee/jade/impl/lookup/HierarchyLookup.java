package snownee.jade.impl.lookup;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.IdMapper;
import net.minecraft.resources.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.PriorityStore;
import snownee.jade.impl.WailaCommonRegistration;

public class HierarchyLookup<T extends IJadeProvider> implements IHierarchyLookup<T> {
	private final Class<?> baseClass;
	private final Cache<Class<?>, List<T>> resultCache = CacheBuilder.newBuilder().build();
	private final boolean singleton;
	protected boolean idMapped;
	protected @Nullable IdMapper<T> idMapper;
	private ListMultimap<Class<?>, T> objects = ArrayListMultimap.create();
	protected @Nullable Map<Identifier, T> byKey;

	public HierarchyLookup(Class<?> baseClass) {
		this(baseClass, false);
	}

	public HierarchyLookup(Class<?> baseClass, boolean singleton) {
		this.baseClass = baseClass;
		this.singleton = singleton;
	}

	@Override
	public void idMapped() {
		idMapped = true;
		keyed();
	}

	@Override
	public IdMapper<T> idMapper() {
		return Objects.requireNonNull(idMapper);
	}

	@Override
	public void register(Class<?> clazz, T provider) {
		Preconditions.checkArgument(isClassAcceptable(clazz), "Class %s is not acceptable", clazz);
		Objects.requireNonNull(provider.getUid());
		WailaCommonRegistration.instance().priorities.put(provider);
		objects.put(clazz, provider);
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
		return baseClass.isAssignableFrom(clazz);
	}

	@Override
	public List<T> get(Class<?> clazz) {
		try {
			return resultCache.get(
					clazz, () -> {
						List<T> list = Lists.newArrayList();
						getInternal(clazz, list);
						list = ImmutableList.sortedCopyOf(COMPARATOR, list);
						if (singleton && !list.isEmpty()) {
							return ImmutableList.of(list.getFirst());
						}
						return list;
					});
		} catch (ExecutionException e) {
			Jade.LOGGER.error("", e);
		}
		return List.of();
	}

	@Override
	public void keyed() {
		byKey = Maps.newHashMap();
	}

	@Override
	public @Nullable T byKey(Identifier key) {
		return Objects.requireNonNull(byKey).get(key);
	}

	private void getInternal(Class<?> clazz, List<T> list) {
		if (clazz != baseClass && clazz != Object.class) {
			getInternal(clazz.getSuperclass(), list);
		}
		list.addAll(objects.get(clazz));
	}

	@Override
	public boolean isEmpty() {
		return objects.isEmpty();
	}

	@Override
	public Stream<Map.Entry<Class<?>, Collection<T>>> entries() {
		return objects.asMap().entrySet().stream();
	}

	@Override
	public void invalidate() {
		resultCache.invalidateAll();
	}

	@Override
	public void loadComplete(PriorityStore<Identifier, IJadeProvider> priorityStore) {
		objects.asMap().forEach((clazz, list) -> {
			if (list.size() < 2) {
				return;
			}
			Set<Identifier> set = Sets.newHashSetWithExpectedSize(list.size());
			for (T provider : list) {
				if (set.contains(provider.getUid())) {
					throw new IllegalStateException("Duplicate UID: %s for %s".formatted(
							provider.getUid(), list.stream()
									.filter(p -> p.getUid().equals(provider.getUid()))
									.map(p -> p.getClass().getName())
									.toList()));
				}
				set.add(provider.getUid());
			}
		});

		objects = ImmutableListMultimap.<Class<?>, T>builder()
				.orderValuesBy(Comparator.comparingInt(priorityStore::byValue))
				.putAll(objects)
				.build();

		if (idMapped) {
			idMapper = createIdMapper();
		}
	}

}
