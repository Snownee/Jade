package snownee.jade.api.view;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.FluidTextHelper;

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
	public static FluidView readDefault(Data data) {
		if (data.capacity <= 0) {
			return null;
		}
		JadeFluidObject fluidObject = data.fluid;
		if (fluidObject == null) {
			return null;
		}
		long amount = fluidObject.getAmount();
		FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluidObject));
		fluidView.fluidName = CommonProxy.getFluidName(fluidObject);
		fluidView.current = FluidTextHelper.getUnicodeMillibuckets(amount, true);
		fluidView.max = FluidTextHelper.getUnicodeMillibuckets(data.capacity, true);
		fluidView.ratio = (float) ((double) amount / data.capacity);
		if (fluidObject.getType().isSame(Fluids.EMPTY)) {
			fluidView.overrideText = Component.translatable(
					"jade.fluid",
					EMPTY_FLUID,
					Component.literal(fluidView.max).withStyle(ChatFormatting.GRAY));
		}
		return fluidView;
	}

	public record Data(JadeFluidObject fluid, long capacity) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				JadeFluidObject.STREAM_CODEC,
				Data::fluid,
				ByteBufCodecs.LONG,
				Data::capacity,
				Data::new);
	}

}
