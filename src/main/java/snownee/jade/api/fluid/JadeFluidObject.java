package snownee.jade.api.fluid;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.util.CommonProxy;

public class JadeFluidObject {
	public static final Codec<JadeFluidObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					BuiltInRegistries.FLUID.byNameCodec().fieldOf("type").forGetter(JadeFluidObject::getType),
					Codec.LONG.fieldOf("amount").forGetter(JadeFluidObject::getAmount),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(JadeFluidObject::getComponents))
			.apply(instance, JadeFluidObject::of));

	public static final StreamCodec<RegistryFriendlyByteBuf, JadeFluidObject> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.FLUID),
			JadeFluidObject::getType,
			ByteBufCodecs.LONG,
			JadeFluidObject::getAmount,
			DataComponentPatch.STREAM_CODEC,
			JadeFluidObject::getComponents,
			JadeFluidObject::of);

	public static long bucketVolume() {
		return CommonProxy.bucketVolume();
	}

	public static long blockVolume() {
		return CommonProxy.blockVolume();
	}

	public static JadeFluidObject empty() {
		return of(Fluids.EMPTY, 0);
	}

	public static JadeFluidObject of(Fluid fluid) {
		return of(fluid, blockVolume());
	}

	public static JadeFluidObject of(Fluid fluid, long amount) {
		return of(fluid, amount, DataComponentPatch.EMPTY);
	}

	public static JadeFluidObject of(Fluid fluid, long amount, DataComponentPatch components) {
		return new JadeFluidObject(fluid, amount, components);
	}

	private final Fluid type;
	private final long amount;
	private final DataComponentPatch components;

	private JadeFluidObject(Fluid type, long amount, DataComponentPatch components) {
		this.type = type;
		this.amount = amount;
		this.components = components;
		Objects.requireNonNull(type);
		Objects.requireNonNull(components);
	}

	public Fluid getType() {
		return type;
	}

	public long getAmount() {
		return amount;
	}

	public DataComponentPatch getComponents() {
		return components;
	}

	public boolean isEmpty() {
		return getType() == Fluids.EMPTY || getAmount() == 0;
	}
}
