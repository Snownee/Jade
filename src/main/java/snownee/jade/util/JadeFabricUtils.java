package snownee.jade.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.math.LongMath;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ViewGroup;

public final class JadeFabricUtils {

	private JadeFabricUtils() {
	}

	public static List<ViewGroup<FluidView.Data>> fromFluidStorage(Storage<FluidVariant> storage) {
		FluidCollectingResult result = fromFluidHandlerStream(storage);
		if (result.tanks == 0) {
			return List.of();
		}
		List<Tuple<JadeFluidObject, Long>> list = Lists.newArrayList();
		int maxTanks = result.emptyTanks == 0 ? 5 : 4;
		if (result.tanks - result.emptyTanks <= maxTanks) {
			list.addAll(result.stream.toList());
		} else {
			result.stream.takeWhile(tag -> list.size() <= maxTanks).forEach(tuple1 -> {
				for (Tuple<JadeFluidObject, Long> tuple2 : list) {
					if (JadeFluidObject.isSameFluidSameComponents(tuple1.getA(), tuple2.getA())) {
						return;
					}
				}
				list.add(tuple1);
			});
		}
		int remaining = result.tanks - result.emptyTanks - list.size();
		if (result.emptyTanks > 0) {
			list.add(new Tuple<>(JadeFluidObject.empty(), result.emptyCapacity));
		}
		ViewGroup<FluidView.Data> group = new ViewGroup<>(list.stream()
				.map(tuple -> new FluidView.Data(tuple.getA(), tuple.getB()))
				.toList());
		if (remaining > 0) {
			group.getExtraData().putInt("+", remaining);
		}
		return List.of(group);
	}

	public static FluidCollectingResult fromFluidHandlerStream(Storage<FluidVariant> storage) {
		FluidCollectingResult result = new FluidCollectingResult();
		storage.forEach($ -> {
			if ($.getCapacity() > 0) {
				result.tanks++;
				if ($.isResourceBlank()) {
					result.emptyTanks++;
				}
			}
		});
		if (result.tanks == 0) {
			result.stream = Stream.empty();
		} else {
			result.stream = Streams.stream(storage.iterator()).map($ -> {
				long capacity = $.getCapacity();
				if (capacity <= 0) {
					return null;
				}
				if ($.isResourceBlank()) {
					result.emptyCapacity = LongMath.saturatedAdd(result.emptyCapacity, capacity);
					return null;
				}
				return new Tuple<>(
						JadeFluidObject.of($.getResource().getFluid(), $.getAmount(), $.getResource().getComponents()),
						capacity);
			}).filter(Objects::nonNull);
		}
		return result;
	}

	public static class FluidCollectingResult {
		public Stream<Tuple<JadeFluidObject, Long>> stream = Stream.empty();
		public long emptyCapacity;
		public int tanks;
		public int emptyTanks;
	}

	public static ItemIterator<? extends Storage<ItemVariant>> fromItemStorage(Storage<ItemVariant> storage, int fromIndex) {
		return fromItemStorage(storage, fromIndex, CommonProxy::findItemHandler);
	}

	public static ItemIterator<? extends Storage<ItemVariant>> fromItemStorage(
			Storage<ItemVariant> storage,
			int fromIndex,
			Function<Accessor<?>, @Nullable Storage<ItemVariant>> containerFinder) {
		if (storage instanceof SlottedStorage) {
			return new ItemIterator.SlottedItemIterator<>(
					accessor -> {
						if (containerFinder.apply(accessor) instanceof SlottedStorage<ItemVariant> slotted) {
							return slotted;
						}
						//noinspection DataFlowIssue
						return null;
					}, fromIndex) {
				@Override
				protected int getSlotCount(SlottedStorage<ItemVariant> container) {
					return container.getSlotCount();
				}

				@Override
				protected ItemStack getItemInSlot(SlottedStorage<ItemVariant> container, int slot) {
					SingleSlotStorage<ItemVariant> slotStorage = container.getSlot(slot);
					return slotStorage.getResource().toStack((int) Mth.clamp(slotStorage.getAmount(), 0, Integer.MAX_VALUE));
				}

				@Override
				public long getVersion(SlottedStorage<ItemVariant> container) {
					return container.getVersion();
				}
			};
		} else {
			return new ItemIterator.SlotlessItemIterator<>(containerFinder, fromIndex) {
				@Override
				protected Stream<ItemStack> populateRaw(Storage<ItemVariant> container) {
					return Streams.stream(container.nonEmptyIterator()).map($ -> $.getResource()
							.toStack((int) Mth.clamp($.getAmount(), 0, Integer.MAX_VALUE)));
				}

				@Override
				public long getVersion(Storage<ItemVariant> container) {
					return container.getVersion();
				}
			};
		}
	}

}
