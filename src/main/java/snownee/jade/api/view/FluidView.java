package snownee.jade.api.view;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

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
		} else {
			fluidView.current = IDisplayHelper.get().humanReadableNumber(amount, "B", true);
			fluidView.max = IDisplayHelper.get().humanReadableNumber(capacity, "B", true);
		}
		fluidView.ratio = (float) ((double) amount / capacity);
		return fluidView;
	}

	public static CompoundTag fromFluidVariant(FluidVariant fluidVariant, long amount, long capacity) {
		CompoundTag tag = fluidVariant.toNbt();
		tag.putLong("Amount", amount);
		tag.putLong("Capacity", capacity);
		return tag;
	}

}
