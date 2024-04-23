package snownee.jade.util;

import java.util.OptionalInt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

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

	public static <T> T createFromEmptyMap(Codec<T> codec) {
		return codec.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).get().left().orElseThrow();
	}
}
