package snownee.jade.api.view;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.api.fluid.JadeFluidObject;
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
	public static FluidView readDefault(CompoundTag tag) {
		long capacity = tag.getLong("capacity");
		if (capacity <= 0) {
			return null;
		}
		Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid")));
		CompoundTag nbt = tag.contains("tag") ? tag.getCompound("tag") : null;
		long amount = tag.getLong("amount");
		JadeFluidObject fluidObject = JadeFluidObject.of(fluid, amount, nbt);
		FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluidObject));
		fluidView.fluidName = new FluidStack(fluid, 1, nbt).getDisplayName();
		if (amount <= 0) {
			fluidView.overrideText = EMPTY_FLUID;
		}
		fluidView.current = IDisplayHelper.get().humanReadableNumber(amount, "B", true);
		fluidView.max = IDisplayHelper.get().humanReadableNumber(capacity, "B", true);
		fluidView.ratio = (float) amount / capacity;
		return fluidView;
	}

	public static CompoundTag writeDefault(JadeFluidObject fluidObject, long capacity) {
		CompoundTag tag = new CompoundTag();
		if (capacity <= 0) {
			return tag;
		}
		tag.putString("fluid", BuiltInRegistries.FLUID.getKey(fluidObject.getType()).toString());
		tag.putLong("amount", fluidObject.getAmount());
		tag.putLong("capacity", capacity);
		if (fluidObject.getTag() != null) {
			tag.put("tag", fluidObject.getTag());
		}
		return tag;
	}

}
