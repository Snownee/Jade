package snownee.jade.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import snownee.jade.api.IJadeProvider;

public class HierarchyLookup<T extends IJadeProvider> {

	private final Class<?> baseClass;
	private ListMultimap<Class<?>, T> objects = ArrayListMultimap.create();
	private final Cache<Class<?>, List<T>> resultCache = CacheBuilder.newBuilder().build();

	public HierarchyLookup(Class<?> baseClass) {
		this.baseClass = baseClass;
	}

	public void register(Class<?> clazz, T provider) {
		Preconditions.checkNotNull(clazz);
		Preconditions.checkNotNull(provider.getUid());
		WailaCommonRegistration.INSTANCE.priorities.put(provider.getUid(), provider);
		objects.put(clazz, provider);
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

	public void invalidate() {
		resultCache.invalidateAll();
	}

	public void loadComplete(PriorityStore<IJadeProvider> priorityStore) {
		objects = ImmutableListMultimap.<Class<?>, T>builder().orderValuesBy(Comparator.comparingInt(priorityStore::get)).putAll(objects).build();
	}

}
