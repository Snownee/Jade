package snownee.jade.api.callback;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Consumer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntReferenceImmutablePair;
import it.unimi.dsi.fastutil.ints.IntReferencePair;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

public class CallbackContainer<T> {

	private final SortedSet<IntReferencePair<T>> callbacks = new ObjectRBTreeSet<>(Comparator.comparing(IntReferencePair::firstInt));
	private final LoadingCache<Boolean, Collection<T>> callbacksView = CacheBuilder.newBuilder().build(new CacheLoader<>() {
		@Override
		public Collection<T> load(Boolean key) {
			return Collections2.transform(callbacks, Pair::second);
		}
	});

	public void add(T callback) {
		add(0, callback);
	}

	public void add(int priority, T callback) {
		Objects.requireNonNull(callback);
		callbacks.add(IntReferenceImmutablePair.of(priority, callback));
		callbacksView.invalidateAll();
	}

	public Collection<T> callbacks() {
		return callbacksView.getUnchecked(Boolean.TRUE);
	}

	public void call(Consumer<T> consumer) {
		for (T callback : callbacks()) {
			consumer.accept(callback);
		}
	}
}
