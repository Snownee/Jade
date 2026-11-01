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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ViewGroup;

/**
 * NeoForge exposes inventories and tanks through its transfer API
 * ({@code ResourceHandler<ItemResource>} / {@code ResourceHandler<FluidResource>}), which Forge
 * 26.2 does not have. The equivalents here are the capability interfaces Forge has always used,
 * {@link IItemHandler} and {@link IFluidHandler}; the shape of the code is otherwise unchanged.
 */
public class JadeForgeUtils {

	private JadeForgeUtils() {
	}

	/**
	 * Forge's FluidStack carries its variant data as an NBT tag rather than a DataComponentPatch,
	 * so the component-aware overload of {@link JadeFluidObject#of} cannot be used. Fluids that
	 * differ only by NBT therefore render as the same fluid in the tooltip.
	 */
	public static JadeFluidObject fromFluidStack(FluidStack stack) {
		return JadeFluidObject.of(stack.getFluid(), stack.getAmount());
	}

	public static List<ViewGroup<FluidView.Data>> fromFluidHandler(IFluidHandler storage) {
		if (storage.getTanks() == 0) {
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

	public static FluidCollectingResult fromFluidHandlerStream(IFluidHandler fluidHandler) {
		FluidCollectingResult result = new FluidCollectingResult();
		for (int i = 0; i < fluidHandler.getTanks(); i++) {
			if (fluidHandler.getTankCapacity(i) > 0) {
				result.tanks++;
				if (fluidHandler.getFluidInTank(i).isEmpty()) {
					result.emptyTanks++;
				}
			}
		}
		if (result.tanks == 0) {
			result.stream = Stream.empty();
		} else {
			result.stream = IntStream.range(0, fluidHandler.getTanks()).mapToObj(i -> {
				long capacity = fluidHandler.getTankCapacity(i);
				if (capacity <= 0) {
					return null;
				}
				FluidStack stack = fluidHandler.getFluidInTank(i);
				if (stack.isEmpty()) {
					result.emptyCapacity = LongMath.saturatedAdd(result.emptyCapacity, capacity);
					return null;
				}
				return new Pair<>(fromFluidStack(stack), capacity);
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

	public static ItemIterator<? extends IItemHandler> fromItemHandler(
			IItemHandler storage,
			int fromIndex) {
		return fromItemHandler(storage, fromIndex, CommonProxy::findItemHandler);
	}

	public static ItemIterator<? extends IItemHandler> fromItemHandler(
			IItemHandler storage,
			int fromIndex,
			Function<Accessor<?>, @Nullable IItemHandler> containerFinder) {
		return new ItemIterator.SlottedItemIterator<>(containerFinder, fromIndex) {
			@Override
			protected int getSlotCount(IItemHandler container) {
				return container.getSlots();
			}

			@Override
			protected ItemStack getItemInSlot(IItemHandler container, int slot) {
				return container.getStackInSlot(slot);
			}
		};
	}
}
