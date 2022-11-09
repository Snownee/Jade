package snownee.jade.api.view;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
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
		int capacity = tag.getInt("Capacity");
		if (capacity <= 0) {
			return null;
		}
		FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
		FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluid));
		fluidView.fluidName = fluid.getDisplayName();
		if (fluid.isEmpty()) {
			fluidView.overrideText = EMPTY_FLUID;
		} else {
			fluidView.current = IDisplayHelper.get().humanReadableNumber(fluid.getAmount(), "B", true);
			fluidView.max = IDisplayHelper.get().humanReadableNumber(capacity, "B", true);
		}
		fluidView.ratio = (float) fluid.getAmount() / capacity;
		return fluidView;
	}

	public static CompoundTag fromFluidStack(FluidStack fluidStack, int capacity) {
		CompoundTag tag = fluidStack.writeToNBT(new CompoundTag());
		tag.putInt("Capacity", capacity);
		return tag;
	}

}
