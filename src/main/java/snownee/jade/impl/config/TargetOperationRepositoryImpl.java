package snownee.jade.impl.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.config.IgnoreList;
import snownee.jade.api.config.TargetOperationRepository;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeCodecs;
import snownee.jade.util.JsonConfig;

public class TargetOperationRepositoryImpl<T, U> implements TargetOperationRepository<T, U> {
	private final ResourceKey<? extends Registry<T>> registry;
	private final Function<U, ResourceKey<T>> mapper;
	private final String fileName;
	private final Supplier<List<String>> defaultValues;
	private final Map<ResourceKey<T>, TargetOperation> builtIn = Maps.newIdentityHashMap();
	private final Map<ResourceKey<T>, TargetOperation> operations = Maps.newIdentityHashMap();

	public TargetOperationRepositoryImpl(
			ResourceKey<? extends Registry<T>> registry,
			Function<U, ResourceKey<T>> mapper,
			String fileName,
			Supplier<List<String>> defaultValues) {
		this.registry = registry;
		this.mapper = mapper;
		this.fileName = fileName;
		this.defaultValues = defaultValues;
	}

	@Override
	public void reload(HolderLookup.Provider provider) {
		HolderLookup.RegistryLookup<T> lookup = provider.lookupOrThrow(registry);
		operations.clear();
		operations.putAll(builtIn);

		IgnoreList list = new JsonConfig<>(
				Jade.ID + "/" + fileName,
				JadeCodecs.ignoreList(),
				null,
				() -> Util.make(new IgnoreList(), $ -> $.values = defaultValues.get())).get();
		List<Pattern> patterns = Lists.newArrayList();
		for (String value : list.values) {
			try {
				if (value.startsWith("/") && value.endsWith("/") && value.length() > 1) {
					patterns.add(Pattern.compile(value.substring(1, value.length() - 1)));
				} else {
					ResourceKey<T> key = ResourceKey.create(registry, ResourceLocation.parse(value));
					Optional<Holder.Reference<T>> optional = lookup.get(key);
					if (optional.isPresent()) {
						operations.put(optional.get().key(), TargetOperation.HIDE);
					} else {
						throw new IllegalArgumentException("Unknown id: " + value);
					}
				}
			} catch (Exception e) {
				Jade.LOGGER.error("Failed to parse ignore list entry: %s".formatted(value), e);
			}
		}
		if (!patterns.isEmpty()) {
			for (ResourceKey<T> key : lookup.listElementIds().toList()) {
				String s = key.location().toString();
				for (Pattern pattern : patterns) {
					if (pattern.matcher(s).find()) {
						operations.put(key, TargetOperation.HIDE);
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean shouldHide(ResourceKey<T> key) {
		return operations.get(key) == TargetOperation.HIDE;
	}

	@Override
	public boolean shouldPick(ResourceKey<T> key) {
		return operations.get(key) == TargetOperation.PICK;
	}

	@Override
	public void hide(ResourceKey<T> key) {
		builtIn.put(Objects.requireNonNull(key), TargetOperation.HIDE);
	}

	@Override
	public void pick(ResourceKey<T> key) {
		if (!CommonProxy.isPhysicallyClient()) {
			return;
		}
		builtIn.put(Objects.requireNonNull(key), TargetOperation.PICK);
	}

	@Override
	public ResourceKey<T> map(U obj) {
		return mapper.apply(obj);
	}
}
