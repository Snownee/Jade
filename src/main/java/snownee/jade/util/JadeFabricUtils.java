package snownee.jade.util;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.math.LongMath;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

public final class JadeFabricUtils {

	private JadeFabricUtils() {
	}

	public static List<ViewGroup<CompoundTag>> fromFluidStorage(Storage<FluidVariant> storage) {
		List<CompoundTag> list = Lists.newArrayList();
		long emptyCapacity = 0;
		try (Transaction outer = Transaction.openOuter()) {
			for (var view : storage.iterable(outer)) {
				long capacity = view.getCapacity();
				if (capacity <= 0)
					continue;
				if (view.isResourceBlank() || view.getAmount() <= 0) {
					emptyCapacity = LongMath.saturatedAdd(emptyCapacity, capacity);
					continue;
				}
				list.add(FluidView.writeDefault(JadeFluidObject.of(view.getResource().getFluid(), view.getAmount(), view.getResource().getNbt()), capacity));
			}
		}
		if (list.isEmpty() && emptyCapacity > 0) {
			list.add(FluidView.writeDefault(JadeFluidObject.empty(), emptyCapacity));
		}
		if (!list.isEmpty()) {
			return List.of(new ViewGroup<>(list));
		}
		return List.of();
	}

	public static ViewGroup<ItemStack> fromItemStorage(Storage<ItemVariant> storage, int maxSize, int startIndex) {
		try (Transaction outer = Transaction.openOuter()) {
			return ItemView.compacted(Streams.stream(storage.iterator(outer)).skip(startIndex).limit(maxSize * 3).map($ -> {
				return $.getResource().toStack((int) Mth.clamp($.getAmount(), 0, Integer.MAX_VALUE));
			}), maxSize);
		}
	}

}
