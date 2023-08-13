package snownee.jade.addon.universal;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class ItemIterator<T> {
	protected final Function<Object, @Nullable T> containerFinder;
	protected boolean finished;

	protected ItemIterator(Function<Object, @Nullable T> containerFinder) {
		this.containerFinder = containerFinder;
	}

	public @Nullable T find(Object target) {
		return containerFinder.apply(target);
	}

	public final boolean isFinished() {
		return finished;
	}

	public abstract long getVersion(T container);

	public abstract Stream<ItemStack> populate(T container);

	public abstract void reset();

	public static abstract class SlottedItemIterator<T> extends ItemIterator<T> {
		public static final AtomicLong version = new AtomicLong();
		private final int fromIndex;
		private int currentIndex;

		public SlottedItemIterator(Function<Object, @Nullable T> containerFinder, int fromIndex) {
			super(containerFinder);
			this.currentIndex = this.fromIndex = fromIndex;
		}

		protected abstract int getSlotCount(T container);

		protected abstract ItemStack getItemInSlot(T container, int slot);

		@Override
		public void reset() {
			currentIndex = fromIndex;
			finished = false;
		}

		@Override
		public Stream<ItemStack> populate(T container) {
			int slotCount = getSlotCount(container);
			int toIndex = currentIndex + ItemStorageCache.MAX_SIZE;
			if (toIndex >= slotCount) {
				toIndex = slotCount;
				finished = true;
			}
			int fromIndex = currentIndex;
			currentIndex = toIndex;
			return IntStream.range(fromIndex, toIndex).mapToObj(slot -> getItemInSlot(container, slot));
		}

		@Override
		public long getVersion(T container) {
			return version.getAndIncrement();
		}
	}

	public static class ContainerItemIterator extends SlottedItemIterator<Container> {
		public ContainerItemIterator(int fromIndex) {
			this(Container.class::cast, fromIndex);
		}

		public ContainerItemIterator(Function<Object, @Nullable Container> containerFinder, int fromIndex) {
			super(containerFinder, fromIndex);
		}

		@Override
		protected int getSlotCount(Container container) {
			return container.getContainerSize();
		}

		@Override
		protected ItemStack getItemInSlot(Container container, int slot) {
			return container.getItem(slot);
		}
	}

	public static abstract class SlotlessItemIterator<T> extends ItemIterator<T> {
		private final int fromIndex;

		protected SlotlessItemIterator(Function<Object, @Nullable T> containerFinder, int fromIndex) {
			super(containerFinder);
			this.fromIndex = fromIndex;
		}

		@Override
		public Stream<ItemStack> populate(T container) {
			return populateRaw(container).skip(fromIndex).limit(ItemStorageCache.MAX_SIZE * 3L);
		}

		protected abstract Stream<ItemStack> populateRaw(T container);

		@Override
		public void reset() {
			finished = false;
		}
	}
}
