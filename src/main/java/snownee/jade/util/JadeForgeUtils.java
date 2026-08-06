package snownee.jade.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.math.LongMath;
import com.mojang.serialization.DynamicOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ViewGroup;

public class JadeForgeUtils {

	private JadeForgeUtils() {
	}

	public static ItemIterator<? extends IItemHandler> fromItemHandler(IItemHandler storage, int fromIndex) {
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

	public static JadeFluidObject fromFluidStack(FluidStack fluidStack) {
		return JadeFluidObject.of(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getComponentsPatch());
	}

	@Deprecated
	public static List<ViewGroup<CompoundTag>> fromFluidHandler(IFluidHandler fluidHandler) {
		return fromFluidHandler(fluidHandler, NbtOps.INSTANCE);
	}

	public static List<ViewGroup<CompoundTag>> fromFluidHandler(IFluidHandler fluidHandler, DynamicOps<Tag> ops) {
		FluidCollectingResult result = fromFluidHandlerStream(fluidHandler);
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
		ViewGroup<CompoundTag> group = new ViewGroup<>(list.stream()
				.map(tuple -> FluidView.writeDefault(tuple.getA(), tuple.getB(), ops))
				.toList());
		if (remaining > 0) {
			group.getExtraData().putInt("+", remaining);
		}
		return List.of(group);
	}

	public static FluidCollectingResult fromFluidHandlerStream(IFluidHandler fluidHandler) {
		FluidCollectingResult result = new FluidCollectingResult();
		for (int i = 0; i < fluidHandler.getTanks(); i++) {
			int capacity = fluidHandler.getTankCapacity(i);
			if (capacity > 0) {
				result.tanks++;
				if (fluidHandler.getFluidInTank(i).isEmpty()) {
					result.emptyTanks++;
					result.emptyCapacity = LongMath.saturatedAdd(result.emptyCapacity, capacity);
				}
			}
		}
		if (result.tanks == 0) {
			result.stream = Stream.empty();
		} else {
			result.stream = IntStream.range(0, fluidHandler.getTanks()).mapToObj(i -> {
				int capacity = fluidHandler.getTankCapacity(i);
				if (capacity <= 0) {
					return null;
				}
				FluidStack fluidStack = fluidHandler.getFluidInTank(i);
				if (fluidStack.isEmpty()) {
					return null;
				}
				return new Tuple<>(fromFluidStack(fluidStack), (long) capacity);
			}).filter(Objects::nonNull);
		}
		return result;
	}

	public static class FluidCollectingResult {
		public Stream<Tuple<JadeFluidObject, Long>> stream;
		public long emptyCapacity;
		public int tanks;
		public int emptyTanks;
	}
}
