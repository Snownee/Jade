package snownee.jade.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IJadeProvider;
import snownee.jade.util.CommonProxy;

public class HierarchyLookup<T extends IJadeProvider> {

	private final Class<?> baseClass;
	private ListMultimap<Class<?>, T> objects = ArrayListMultimap.create();
	private final Cache<Class<?>, List<T>> resultCache = CacheBuilder.newBuilder().build();
	private final boolean singleton;

	public HierarchyLookup(Class<?> baseClass) {
		this(baseClass, false);
	}

	public HierarchyLookup(Class<?> baseClass, boolean singleton) {
		this.baseClass = baseClass;
		this.singleton = singleton;
	}

	public void register(Class<?> clazz, T provider) {
		if (CommonProxy.isBlockedUid(provider)) {
			return;
		}
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(provider.getUid());
		WailaCommonRegistration.INSTANCE.priorities.put(provider);
		objects.put(clazz, provider);
	}

	public List<T> get(Object obj) {
		if (obj == null) {
			return List.of();
		}
		return get(obj.getClass());
	}

	public List<T> get(Class<?> clazz) {
		try {
			return resultCache.get(clazz, () -> {
				List<T> list = Lists.newArrayList();
				getInternal(clazz, list);
				list = ImmutableList.sortedCopyOf(Comparator.comparingInt(WailaCommonRegistration.INSTANCE.priorities::byValue), list);
				if (singleton && !list.isEmpty())
					return ImmutableList.of(list.get(0));
				return list;
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return List.of();
	}

	private void getInternal(Class<?> clazz, List<T> list) {
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

	public void loadComplete(PriorityStore<ResourceLocation, IJadeProvider> priorityStore) {
		objects = ImmutableListMultimap.<Class<?>, T>builder().orderValuesBy(Comparator.comparingInt(priorityStore::byValue)).putAll(objects).build();
	}

}
