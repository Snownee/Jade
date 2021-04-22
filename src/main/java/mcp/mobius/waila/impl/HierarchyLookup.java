package mcp.mobius.waila.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class HierarchyLookup<T> {

	private final Class<?> baseClass;
	private final Multimap<Class<?>, T> objects = Multimaps.newMultimap(Maps.newHashMap(), Sets::newLinkedHashSet);
	private final Cache<Class<?>, List<T>> resultCache = CacheBuilder.newBuilder().build();

	public HierarchyLookup(Class<?> baseClass) {
		this.baseClass = baseClass;
	}

	public void register(Class<?> clazz, T dataProvider) {
		Preconditions.checkNotNull(clazz);
		Preconditions.checkNotNull(dataProvider);
		objects.put(clazz, dataProvider);
	}

	public List<T> get(Object obj) {
		if (obj == null) {
			return Collections.EMPTY_LIST;
		}
		return get(obj.getClass());
	}

	public List<T> get(Class<?> clazz) {
		try {
			return resultCache.get(clazz, () -> {
				ImmutableList.Builder<T> list = ImmutableList.builder();
				getInternal(clazz, list);
				return list.build();
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	private void getInternal(Class<?> clazz, ImmutableList.Builder<T> list) {
		if (clazz != baseClass && clazz != Object.class) {
			getInternal(clazz.getSuperclass(), list);
		}
		list.addAll(objects.get(clazz));
	}

	public Multimap<Class<?>, T> getObjects() {
		return objects;
	}
}
