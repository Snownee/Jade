package snownee.jade.impl;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.PlatformProxy;

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
		Path saveFile = PlatformProxy.getConfigDirectory().toPath().resolve(fileName + ".json");
		Map<ResourceLocation, Integer> map = null;
		if (Files.exists(saveFile)) {
			try (final BufferedReader reader = Files.newBufferedReader(saveFile, Charsets.UTF_8)) {
				@SuppressWarnings("serial")
				Type type = new TypeToken<LinkedHashMap<ResourceLocation, Integer>>() {
				}.getType();
				map = JsonConfig.DEFAULT_GSON.fromJson(reader, type);
			} catch (Exception e) {
				Jade.LOGGER.catching(e);
			}
		}
		if (map == null) {
			map = Maps.newLinkedHashMap();
		} else {
			for (var e : map.entrySet()) {
				if (e.getValue() != null)
					priorities.put(e.getKey(), e.getValue().intValue());
			}
		}
		Map<ResourceLocation, Integer> map0 = map;
		new Thread(() -> {
			for (ResourceLocation id : priorities.keySet()) {
				if (!map0.containsKey(id)) {
					map0.put(id, null);
				}
			}
			try {
				Files.write(saveFile, JsonConfig.DEFAULT_GSON.toJson(map0).getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				Jade.LOGGER.catching(e);
			}
		}).start();

		List<ResourceLocation> keys = allKeys.stream()
				.filter($ -> !$.getPath().contains("."))
				.sorted(Comparator.comparingInt(this::get))
				.collect(Collectors.toCollection(LinkedList::new));
		allKeys.stream()
				.filter($ -> $.getPath().contains("."))
				.forEach($ -> {
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
