package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.util.FluidTextHelper;

/**
 * Client-side representation of a fluid storage view.
 */
public class FluidView {

	public static final Component EMPTY_FLUID = Component.translatable("jade.fluid.empty");

	/**
	 * Overlay element rendered for the fluid.
	 */
	public Element overlay;
	/**
	 * Current amount text.
	 */
	public Component current;
	/**
	 * Maximum amount text.
	 */
	public Component max;
	/**
	 * Fill ratio.
	 */
	public float ratio;
	/**
	 * Optional fluid name.
	 */
	@Nullable
	public Component fluidName;
	/**
	 * Optional override text.
	 */
	@Nullable
	public Component overrideText;

	/**
	 * Creates a fluid view.
	 *
	 * @param overlay overlay element
	 * @param current current amount text
	 * @param max maximum amount text
	 */
	public FluidView(Element overlay, Component current, Component max) {
		this.overlay = Objects.requireNonNull(overlay);
		this.current = Objects.requireNonNull(current);
		this.max = Objects.requireNonNull(max);
	}

	/**
	 * Builds a default fluid view from serialized data.
	 *
	 * @param data serialized fluid storage data
	 * @return fluid view, or {@code null} if the data is not renderable
	 */
	@Nullable
	public static FluidView readDefault(Data data) {
		if (data.capacity <= 0 || data.fluids.size() > 1) {
			return null;
		}
		JadeFluidObject fluidObject = data.fluids.isEmpty() ? JadeFluidObject.empty() : data.fluids.getFirst();
		long amount = fluidObject.getAmount();
		Component current = FluidTextHelper.getMillibuckets(amount, true);
		Component max = FluidTextHelper.getMillibuckets(data.capacity, true);
		FluidView view = new FluidView(JadeUI.fluid(fluidObject), current, max);
		view.fluidName = fluidObject.getDisplayName();
		view.ratio = (float) ((double) amount / data.capacity);
		if (fluidObject.is(Fluids.EMPTY)) {
			view.overrideText = NarratableComponent.translatable(
					"jade.fluid",
					EMPTY_FLUID,
					NarratableComponent.attach(Component.literal(view.max.getString()), view.max));
		}
		return view;
	}

	/**
	 * Serialized fluid storage data.
	 */
	public record Data(List<JadeFluidObject> fluids, long capacity) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				JadeFluidObject.STREAM_CODEC.apply(ByteBufCodecs.list()),
				Data::fluids,
				ByteBufCodecs.LONG,
				Data::capacity,
				Data::new);

		/**
		 * Creates a single-fluid payload.
		 *
		 * @param fluid fluid stack
		 * @param capacity storage capacity
		 */
		public Data(JadeFluidObject fluid, long capacity) {
			this(List.of(fluid), capacity);
		}
	}

}
