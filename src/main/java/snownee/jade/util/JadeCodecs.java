package snownee.jade.util;

import java.util.OptionalInt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import snownee.jade.api.config.TargetBlocklist;

public class JadeCodecs {

	public static final Codec<OptionalInt> OPTIONAL_INT = new Codec<>() {
		@Override
		public <T> DataResult<Pair<OptionalInt, T>> decode(DynamicOps<T> ops, T input) {
			return DataResult.success(ops.getNumberValue(input)
										 .map(number -> Pair.of(OptionalInt.of(number.intValue()), ops.empty()))
										 .get()
										 .left()
										 .orElseGet(() -> Pair.of(OptionalInt.empty(), ops.empty())));
		}

		@Override
		public <T> DataResult<T> encode(OptionalInt input, DynamicOps<T> ops, T prefix) {
			if (input.isPresent()) {
				return DataResult.success(ops.createInt(input.getAsInt()));
			} else {
				return DataResult.success(ops.empty());
			}
		}
	};
	public static final Codec<TargetBlocklist> TARGET_BLOCKLIST_CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("__comment", "").forGetter($ -> $.__comment),
			Codec.STRING.listOf().fieldOf("values").forGetter($ -> $.values),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("version", 1).forGetter($ -> $.version)
	).apply(i, (comment, values, version) -> Util.make(new TargetBlocklist(), it -> {
		it.values = values;
		it.version = version;
	})));

	public static <T> T createFromEmptyMap(Codec<T> codec) {
		return codec.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).get().left().orElseThrow();
	}
}
