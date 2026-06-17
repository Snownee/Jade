package snownee.jade.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.math.LongMath;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ViewGroup;

public class JadeForgeUtils {

	private JadeForgeUtils() {
	}

	public static JadeFluidObject fromFluidResource(FluidResource resource, long amount) {
		return JadeFluidObject.of(resource.getFluid(), amount, resource.getComponentsPatch());
	}

	public static List<ViewGroup<FluidView.Data>> fromFluidHandler(ResourceHandler<FluidResource> storage) {
		if (storage.size() == 0) {
			return List.of();
		}
		FluidCollectingResult result = fromFluidHandlerStream(storage);
		if (result.tanks == 0) {
			return List.of();
		}
		List<Pair<JadeFluidObject, Long>> list = Lists.newArrayList();
		int maxTanks = result.emptyTanks == 0 ? 5 : 4;
		if (result.tanks - result.emptyTanks <= maxTanks) {
			list.addAll(result.stream.toList());
		} else {
			result.stream.takeWhile(tag -> list.size() <= maxTanks).forEach(Pair1 -> {
				for (Pair<JadeFluidObject, Long> Pair2 : list) {
					if (JadeFluidObject.isSameFluidSameComponents(Pair1.getFirst(), Pair2.getFirst())) {
						return;
					}
				}
				list.add(Pair1);
			});
		}
		int remaining = result.tanks - result.emptyTanks - list.size();
		if (result.emptyTanks > 0) {
			list.add(new Pair<>(JadeFluidObject.empty(), result.emptyCapacity));
		}
		ViewGroup<FluidView.Data> group = new ViewGroup<>(list.stream()
				.map(Pair -> new FluidView.Data(Pair.getFirst(), Pair.getSecond()))
				.toList());
		if (remaining > 0) {
			group.getExtraData().putInt("+", remaining);
		}
		return List.of(group);
	}

	public static FluidCollectingResult fromFluidHandlerStream(ResourceHandler<FluidResource> fluidHandler) {
		FluidCollectingResult result = new FluidCollectingResult();
		for (int i = 0; i < fluidHandler.size(); i++) {
			if (fluidHandler.getCapacityAsLong(i, FluidResource.EMPTY) > 0) {
				result.tanks++;
				if (fluidHandler.getResource(i).isEmpty()) {
					result.emptyTanks++;
				}
			}
		}
		if (result.tanks == 0) {
			result.stream = Stream.empty();
		} else {
			result.stream = IntStream.range(0, fluidHandler.size()).mapToObj(i -> {
				long capacity = fluidHandler.getCapacityAsLong(i, FluidResource.EMPTY);
				if (capacity <= 0) {
					return null;
				}
				FluidResource resource = fluidHandler.getResource(i);
				if (resource.isEmpty()) {
					result.emptyCapacity = LongMath.saturatedAdd(result.emptyCapacity, capacity);
					return null;
				}
				return new Pair<>(fromFluidResource(resource, fluidHandler.getAmountAsLong(i)), capacity);
			}).filter(Objects::nonNull);
		}
		return result;
	}

	public static class FluidCollectingResult {
		public Stream<Pair<JadeFluidObject, Long>> stream = Stream.empty();
		public long emptyCapacity;
		public int tanks;
		public int emptyTanks;
	}

	public static ItemIterator<? extends ResourceHandler<ItemResource>> fromItemHandler(
			ResourceHandler<ItemResource> storage,
			int fromIndex) {
		return fromItemHandler(storage, fromIndex, CommonProxy::findItemHandler);
	}

	public static ItemIterator<? extends ResourceHandler<ItemResource>> fromItemHandler(
			ResourceHandler<ItemResource> storage,
			int fromIndex,
			Function<Accessor<?>, @Nullable ResourceHandler<ItemResource>> containerFinder) {
		return new ItemIterator.SlottedItemIterator<>(containerFinder, fromIndex) {
			@Override
			protected int getSlotCount(ResourceHandler<ItemResource> container) {
				return container.size();
			}

			@Override
			protected ItemStack getItemInSlot(ResourceHandler<ItemResource> container, int slot) {
				return container.getResource(slot).toStack(container.getAmountAsInt(slot));
			}
		};
	}
}