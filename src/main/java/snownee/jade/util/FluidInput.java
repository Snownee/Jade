package snownee.jade.util;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.DynamicOps;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import snownee.jade.api.fluid.JadeFluidObject;

public class FluidInput {
	private final Holder<Fluid> fluid;
	private final DataComponentPatch components;

	public FluidInput(JadeFluidObject fluidObject) {
		this(fluidObject.getType(), fluidObject.getComponents());
	}

	public FluidInput(Holder<Fluid> holder, DataComponentPatch dataComponentPatch) {
		this.fluid = holder;
		this.components = dataComponentPatch;
	}

	public Fluid getFluid() {
		return this.fluid.value();
	}

	public String serialize(HolderLookup.Provider provider) {
		StringBuilder stringBuilder = new StringBuilder(this.getFluidName());
		String string = this.serializeComponents(provider);
		if (!string.isEmpty()) {
			stringBuilder.append('[');
			stringBuilder.append(string);
			stringBuilder.append(']');
		}

		return stringBuilder.toString();
	}

	private String serializeComponents(HolderLookup.Provider provider) {
		DynamicOps<Tag> dynamicOps = provider.createSerializationContext(NbtOps.INSTANCE);
		return this.components.entrySet().stream().flatMap((entry) -> {
			DataComponentType<?> dataComponentType = entry.getKey();
			ResourceLocation resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType);
			if (resourceLocation == null) {
				return Stream.empty();
			} else {
				Optional<?> optional = entry.getValue();
				if (optional.isPresent()) {
					TypedDataComponent<?> typedDataComponent = TypedDataComponent.createUnchecked(dataComponentType, optional.get());
					return typedDataComponent.encodeValue(dynamicOps).result().stream().map((tag) -> {
						String var10000 = resourceLocation.toString();
						return var10000 + "=" + tag;
					});
				} else {
					return Stream.of("!" + resourceLocation);
				}
			}
		}).collect(Collectors.joining(String.valueOf(',')));
	}

	private String getFluidName() {
		return this.fluid.unwrapKey()
				.map(ResourceKey::location)
				.orElseGet(() -> ResourceLocation.parse("unknown[" + this.fluid + "]"))
				.toString();
	}
}
