package snownee.jade.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.math.LongMath;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ViewGroup;

public final class JadeFabricUtils {

	private JadeFabricUtils() {
	}

	public static List<ViewGroup<CompoundTag>> fromFluidStorage(Storage<FluidVariant> storage) {
		List<CompoundTag> list = Lists.newArrayList();
		long emptyCapacity = 0;
		for (var view : storage) {
			long capacity = view.getCapacity();
			if (capacity <= 0) {
				continue;
			}
			if (view.isResourceBlank() || view.getAmount() <= 0) {
				emptyCapacity = LongMath.saturatedAdd(emptyCapacity, capacity);
				continue;
			}
			list.add(FluidView.writeDefault(JadeFluidObject.of(
					view.getResource().getFluid(),
					view.getAmount(),
					view.getResource().getComponents()), capacity));
		}
		if (list.isEmpty() && emptyCapacity > 0) {
			list.add(FluidView.writeDefault(JadeFluidObject.empty(), emptyCapacity));
		}
		if (!list.isEmpty()) {
			return List.of(new ViewGroup<>(list));
		}
		return List.of();
	}

	public static ItemIterator<? extends Storage<ItemVariant>> fromItemStorage(Storage<ItemVariant> storage, int fromIndex) {
		return fromItemStorage(storage, fromIndex, target -> {
			if (target instanceof BlockEntity be) {
				return ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
			}
			return null;
		});
	}

	public static ItemIterator<? extends Storage<ItemVariant>> fromItemStorage(
			Storage<ItemVariant> storage,
			int fromIndex,
			Function<Object, @Nullable Storage<ItemVariant>> containerFinder) {
		if (storage instanceof SlottedStorage) {
			return new ItemIterator.SlottedItemIterator<>(target -> {
				if (containerFinder.apply(target) instanceof SlottedStorage<ItemVariant> slotted) {
					return slotted;
				}
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
