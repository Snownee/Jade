package snownee.jade.impl.lookup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.PriorityStore;

public class WrappedHierarchyLookup<T extends IJadeProvider> extends HierarchyLookup<T> {
	public final List<Pair<IHierarchyLookup<T>, Function<Accessor<?>, @Nullable Object>>> overrides = Lists.newArrayList();
	private boolean empty = true;

	public WrappedHierarchyLookup() {
		super(Object.class);
	}

	public static <T extends IJadeProvider> WrappedHierarchyLookup<T> forAccessor() {
		WrappedHierarchyLookup<T> lookup = new WrappedHierarchyLookup<>();
		lookup.overrides.add(Pair.of(
				new HierarchyLookup<>(Block.class), accessor -> {
					if (accessor instanceof BlockAccessor blockAccessor) {
						return blockAccessor.getBlock();
					}
					return null;
				}));
		return lookup;
	}

	public List<T> wrappedGet(Accessor<?> accessor) {
		Set<T> set = Sets.newLinkedHashSet();
		for (var override : overrides) {
			Object o = override.getRight().apply(accessor);
			if (o != null) {
				set.addAll(override.getLeft().get(o));
			}
		}
		set.addAll(get(accessor.getTarget()));
		return ImmutableList.sortedCopyOf(COMPARATOR, set);
	}

	public boolean hitsAny(Accessor<?> accessor, BiPredicate<T, Accessor<?>> predicate) {
		if (accessor.isServersideContent()) {
			return true;
		}
		for (T provider : wrappedGet(accessor)) {
			if (predicate.test(provider, accessor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void register(Class<?> clazz, T provider) {
		for (var override : overrides) {
			if (override.getLeft().isClassAcceptable(clazz)) {
				override.getLeft().register(clazz, provider);
				empty = false;
				return;
			}
		}
		super.register(clazz, provider);
		empty = false;
	}

	@Override
	public boolean isClassAcceptable(Class<?> clazz) {
		for (var override : overrides) {
			if (override.getLeft().isClassAcceptable(clazz)) {
				return true;
			}
		}
		return super.isClassAcceptable(clazz);
	}

	@Override
	public void invalidate() {
		for (var override : overrides) {
			override.getLeft().invalidate();
		}
		super.invalidate();
	}

	@Override
	public void loadComplete(PriorityStore<ResourceLocation, IJadeProvider> priorityStore) {
		for (var override : overrides) {
			override.getLeft().loadComplete(priorityStore);
		}
		super.loadComplete(priorityStore);
	}

	@Override
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public Stream<Map.Entry<Class<?>, Collection<T>>> entries() {
		Stream<Map.Entry<Class<?>, Collection<T>>> stream = super.entries();
		for (var override : overrides) {
			stream = Stream.concat(stream, override.getLeft().entries());
		}
		return stream;
	}
}
