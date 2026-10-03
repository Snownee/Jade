package snownee.jade.api.fluid;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.util.CommonProxy;

/**
 * Fluid stack representation used by Jade's client and network codecs.
 */
public class JadeFluidObject implements TypedInstance<Fluid> {
	/**
	 * Codec for serialized fluid objects.
	 */
	public static final Codec<JadeFluidObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("type").forGetter(JadeFluidObject::typeHolder),
					Codec.LONG.fieldOf("amount").forGetter(JadeFluidObject::getAmount),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(JadeFluidObject::getComponents))
			.apply(instance, JadeFluidObject::new));

	/**
	 * Network codec for fluid objects.
	 */
	public static final StreamCodec<RegistryFriendlyByteBuf, JadeFluidObject> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.FLUID),
			JadeFluidObject::typeHolder,
			ByteBufCodecs.LONG,
			JadeFluidObject::getAmount,
			DataComponentPatch.STREAM_CODEC,
			JadeFluidObject::getComponents,
			JadeFluidObject::new);

	/**
	 * Returns the amount represented by one bucket.
	 *
	 * @return bucket volume
	 */
	public static long bucketVolume() {
		return CommonProxy.bucketVolume();
	}

	/**
	 * Returns the amount represented by one block.
	 *
	 * @return block volume
	 */
	public static long blockVolume() {
		return CommonProxy.blockVolume();
	}

	/**
	 * Creates an empty fluid object.
	 *
	 * @return empty fluid object
	 */
	public static JadeFluidObject empty() {
		return of(Fluids.EMPTY, 0);
	}

	/**
	 * Creates a full block-volume fluid object.
	 *
	 * @param fluid fluid type
	 * @return fluid object
	 */
	public static JadeFluidObject of(Fluid fluid) {
		return of(fluid, blockVolume());
	}

	/**
	 * Creates a fluid object with the given amount.
	 *
	 * @param fluid fluid type
	 * @param amount amount in millibuckets
	 * @return fluid object
	 */
	public static JadeFluidObject of(Fluid fluid, long amount) {
		return of(fluid, amount, DataComponentPatch.EMPTY);
	}

	/**
	 * Creates a fluid object with the given amount and components.
	 *
	 * @param fluid fluid type
	 * @param amount amount in millibuckets
	 * @param components attached data components
	 * @return fluid object
	 */
	@SuppressWarnings("deprecation")
	public static JadeFluidObject of(Fluid fluid, long amount, DataComponentPatch components) {
		return new JadeFluidObject(fluid.builtInRegistryHolder(), amount, components);
	}

	private final Holder<Fluid> type;
	private final long amount;
	private final DataComponentPatch components;

	/**
	 * Creates a fluid object.
	 *
	 * @param type fluid holder
	 * @param amount amount in millibuckets
	 * @param components attached data components
	 */
	public JadeFluidObject(Holder<Fluid> type, long amount, DataComponentPatch components) {
		this.type = type;
		this.amount = amount;
		this.components = components;
		Objects.requireNonNull(type);
		Objects.requireNonNull(components);
	}

	/**
	 * Returns the fluid holder.
	 *
	 * @return fluid holder
	 */
	@Override
	public Holder<Fluid> typeHolder() {
		return type;
	}

	/**
	 * Returns the stored amount.
	 *
	 * @return amount in millibuckets
	 */
	public long getAmount() {
		return amount;
	}

	/**
	 * Returns the attached data components.
	 *
	 * @return data component patch
	 */
	public DataComponentPatch getComponents() {
		return components;
	}

	/**
	 * Returns whether this object represents no fluid.
	 *
	 * @return {@code true} if empty
	 */
	public boolean isEmpty() {
		return is(Fluids.EMPTY) || getAmount() == 0;
	}

	/**
	 * Returns the display name for this fluid object.
	 *
	 * @return fluid name
	 */
	public Component getDisplayName() {
		return CommonProxy.getFluidName(this);
	}

	public HoverEvent getTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag) {
		return CommonProxy.getFluidTooltip(this, context, player, flag);
	}

	/**
	 * Returns whether two fluid objects represent the same fluid and matching components.
	 *
	 * @param first first fluid object
	 * @param second second fluid object
	 * @return {@code true} if both objects are equivalent for display
	 */
	public static boolean isSameFluidSameComponents(JadeFluidObject first, JadeFluidObject second) {
		if (first.type != second.type) {
			return false;
		} else {
			return first.isEmpty() && second.isEmpty() || Objects.equals(first.components, second.components);
		}
	}
}
