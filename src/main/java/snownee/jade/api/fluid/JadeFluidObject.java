package snownee.jade.api.fluid;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class JadeFluidObject {
	public static final Codec<JadeFluidObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					BuiltInRegistries.FLUID.byNameCodec().fieldOf("type").forGetter(JadeFluidObject::getType),
					Codec.LONG.fieldOf("amount").forGetter(JadeFluidObject::getAmount),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(JadeFluidObject::getComponents))
			.apply(instance, JadeFluidObject::of));

	public static long bucketVolume() {
		return FluidConstants.BUCKET;
	}

	public static long blockVolume() {
		return FluidConstants.BLOCK;
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
