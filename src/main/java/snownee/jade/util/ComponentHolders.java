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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class ComponentHolders {
	public static String serialize(Holder<?> holder, DataComponentPatch components, HolderLookup.Provider provider) {
		StringBuilder stringBuilder = new StringBuilder(holder.unwrapKey()
				.map(ResourceKey::identifier)
				.orElseGet(() -> Identifier.parse("unknown[" + holder + "]"))
				.toString());
		String string = serializeComponents(components, provider);
		if (!string.isEmpty()) {
			stringBuilder.append('[');
			stringBuilder.append(string);
			stringBuilder.append(']');
		}

		return stringBuilder.toString();
	}

	private static String serializeComponents(DataComponentPatch components, HolderLookup.Provider provider) {
		DynamicOps<Tag> dynamicOps = provider.createSerializationContext(NbtOps.INSTANCE);
		return components.entrySet().stream().flatMap((entry) -> {
			DataComponentType<?> dataComponentType = entry.getKey();
			Identifier Identifier = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType);
			if (Identifier == null) {
				return Stream.empty();
			} else {
				Optional<?> optional = entry.getValue();
				if (optional.isPresent()) {
					TypedDataComponent<?> typedDataComponent = TypedDataComponent.createUnchecked(dataComponentType, optional.get());
					return typedDataComponent.encodeValue(dynamicOps).result().stream().map((tag) -> {
						String var10000 = Identifier.toString();
						return var10000 + "=" + tag;
					});
				} else {
					return Stream.of("!" + Identifier);
				}
			}
		}).collect(Collectors.joining(String.valueOf(',')));
	}
}
