package snownee.jade.addon.universal;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.Accessor;

public abstract class ItemIterator<T> {
	public static final AtomicLong version = new AtomicLong();
	protected final Function<Accessor<?>, @Nullable T> containerFinder;
	protected final int fromIndex;
	protected boolean finished;
	protected int currentIndex;

	protected ItemIterator(Function<Accessor<?>, @Nullable T> containerFinder, int fromIndex) {
		this.containerFinder = containerFinder;
		this.currentIndex = this.fromIndex = fromIndex;
	}

	public @Nullable T find(Accessor<?> accessor) {
		return containerFinder.apply(accessor);
	}

	public final boolean isFinished() {
		return finished;
	}

	public long getVersion(T container) {
		return version.getAndIncrement();
	}

	public abstract Stream<ItemStack> populate(T container);

	public void reset() {
		currentIndex = fromIndex;
		finished = false;
	}

	public void afterPopulate(int count) {
		currentIndex += count;
		if (count == 0 || currentIndex >= 10000) {
			finished = true;
		}
	}

	public float getCollectingProgress() {
		return Float.NaN;
	}

	public static abstract class SlottedItemIterator<T> extends ItemIterator<T> {
		protected float progress;

		public SlottedItemIterator(Function<Accessor<?>, @Nullable T> containerFinder, int fromIndex) {
			super(containerFinder, fromIndex);
		}

		protected abstract int getSlotCount(T container);

		protected abstract ItemStack getItemInSlot(T container, int slot);

		@Override
		public Stream<ItemStack> populate(T container) {
			int slotCount = getSlotCount(container);
			int toIndex = currentIndex + ItemCollector.MAX_SIZE * 2;
			if (toIndex >= slotCount) {
				toIndex = slotCount;
				finished = true;
			}
			progress = (float) (currentIndex - fromIndex) / (slotCount - fromIndex);
			return IntStream.range(currentIndex, toIndex).mapToObj(slot -> getItemInSlot(container, slot));
		}

		@Override
		public float getCollectingProgress() {
			return progress;
		}
	}

	public static class ContainerItemIterator extends SlottedItemIterator<Container> {
		public ContainerItemIterator(int fromIndex) {
			this(accessor -> {
				if (accessor.getTarget() instanceof Container container) {
					return container;
				}
				return null;
			}, fromIndex);
		}

		public ContainerItemIterator(Function<Accessor<?>, @Nullable Container> containerFinder, int fromIndex) {
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

		protected SlotlessItemIterator(Function<Accessor<?>, @Nullable T> containerFinder, int fromIndex) {
			super(containerFinder, fromIndex);
		}

		@Override
		public Stream<ItemStack> populate(T container) {
			return populateRaw(container).skip(currentIndex).limit(ItemCollector.MAX_SIZE * 2L);
		}

		protected abstract Stream<ItemStack> populateRaw(T container);
	}
}
