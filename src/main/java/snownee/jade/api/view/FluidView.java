package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.math.LongMath;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.FluidTextHelper;

@Experimental
public class FluidView {

	public static final Component EMPTY_FLUID = Component.translatable("jade.fluid.empty");

	public IElement overlay;
	public String current;
	public String max;
	public float ratio;
	@Nullable
	public Component fluidName;
	@Nullable
	public Component overrideText;

	public FluidView(IElement overlay) {
		this.overlay = overlay;
		Objects.requireNonNull(overlay);
	}

	@Nullable
	public static FluidView read(CompoundTag tag) {
		long capacity = tag.getLong("Capacity");
		if (capacity <= 0) {
			return null;
		}
		FluidVariant fluid = FluidVariant.fromNbt(tag);
		FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluid.getFluid().defaultFluidState()));
		fluidView.fluidName = FluidVariantAttributes.getName(fluid);
		long amount = tag.getLong("Amount");
		if (amount <= 0) {
			fluidView.overrideText = EMPTY_FLUID;
		}
		fluidView.current = FluidTextHelper.getUnicodeMillibuckets(amount, true);
		fluidView.max = FluidTextHelper.getUnicodeMillibuckets(capacity, true);
		fluidView.ratio = (float) ((double) amount / capacity);
		return fluidView;
	}

	public static CompoundTag fromFluidVariant(FluidVariant fluidVariant, long amount, long capacity) {
		CompoundTag tag = fluidVariant.toNbt();
		tag.putLong("Amount", amount);
		tag.putLong("Capacity", capacity);
		return tag;
	}

	public static List<ViewGroup<CompoundTag>> fromStorage(Storage<FluidVariant> storage) {
		List<CompoundTag> list = Lists.newArrayList();
		long emptyCapacity = 0;
		for (var view : storage) {
			long capacity = view.getCapacity();
			if (capacity <= 0)
				continue;
			if (view.isResourceBlank() || view.getAmount() <= 0) {
				emptyCapacity = LongMath.saturatedAdd(emptyCapacity, capacity);
				continue;
			}
			list.add(fromFluidVariant(view.getResource(), view.getAmount(), capacity));
		}
		if (list.isEmpty() && emptyCapacity > 0) {
			list.add(fromFluidVariant(FluidVariant.blank(), 0, emptyCapacity));
		}
		if (!list.isEmpty()) {
			return List.of(new ViewGroup<>(list));
		}
		return List.of();
	}
}
