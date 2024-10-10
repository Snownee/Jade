package snownee.jade.util;

import java.util.Optional;
import java.util.OptionalInt;

import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import snownee.jade.api.config.IgnoreList;

public class JadeCodecs {

	public static final Codec<OptionalInt> OPTIONAL_INT = new Codec<>() {
		@Override
		public <T> DataResult<Pair<OptionalInt, T>> decode(DynamicOps<T> ops, T input) {
			return DataResult.success(ops.getNumberValue(input)
					.mapOrElse(
							number -> Pair.of(OptionalInt.of(number.intValue()), ops.empty()),
							ignored -> Pair.of(OptionalInt.empty(), ops.empty())));
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

	public static final PrimitiveCodec<Object> PRIMITIVE = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<Object> read(DynamicOps<T> ops, T input) {
			{
				DataResult<Boolean> result = ops.getBooleanValue(input);
				if (result.isSuccess()) {
					return result.map($ -> $);
				}
			}
			{
				DataResult<Number> result = ops.getNumberValue(input);
				if (result.isSuccess()) {
					return result.map($ -> $);
				}
			}
			{
				DataResult<String> result = ops.getStringValue(input);
				if (result.isSuccess()) {
					return result.map($ -> $);
				}
			}
			return DataResult.error(() -> "Not a primitive value: " + input);
		}

		@Override
		public <T> T write(DynamicOps<T> ops, Object value) {
			if (value instanceof Boolean) {
				return ops.createBoolean((Boolean) value);
			} else if (value instanceof Number) {
				return ops.createNumeric((Number) value);
			} else if (value instanceof String) {
				return ops.createString((String) value);
			}
			throw new IllegalArgumentException("Not a primitive value: " + value);
		}
	};
	public static final StreamCodec<ByteBuf, Object> PRIMITIVE_STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull Object decode(ByteBuf buf) {
			byte b = buf.readByte();
			if (b == 0) {
				return false;
			} else if (b == 1) {
				return true;
			} else if (b == 2) {
				return ByteBufCodecs.VAR_INT.decode(buf);
			} else if (b == 3) {
				return ByteBufCodecs.FLOAT.decode(buf);
			} else if (b == 4) {
				return ByteBufCodecs.STRING_UTF8.decode(buf);
			} else if (b > 20) {
				return b - 20;
			}
			throw new IllegalArgumentException("Unknown primitive type: " + b);
		}

		@Override
		public void encode(ByteBuf buf, Object o) {
			switch (o) {
				case Boolean b -> buf.writeByte(b ? 1 : 0);
				case Number n -> {
					float f = n.floatValue();
					if (f != (int) f) {
						buf.writeByte(3);
						ByteBufCodecs.FLOAT.encode(buf, f);
					}
					int i = n.intValue();
					if (i <= Byte.MAX_VALUE - 20 && i >= 0) {
						buf.writeByte(i + 20);
					} else {
						ByteBufCodecs.VAR_INT.encode(buf, i);
					}
				}
				case String s -> {
					buf.writeByte(4);
					ByteBufCodecs.STRING_UTF8.encode(buf, s);
				}
				case Enum<?> anEnum -> {
					buf.writeByte(4);
					ByteBufCodecs.STRING_UTF8.encode(buf, anEnum.name());
				}
				case null -> throw new NullPointerException();
				default -> throw new IllegalArgumentException("Unknown primitive type: %s (%s)".formatted(o, o.getClass()));
			}
		}
	};

	public static <T> Codec<IgnoreList<T>> ignoreList(ResourceKey<? extends Registry<T>> registryKey) {
		return RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.optionalFieldOf("__comment", "").forGetter($ -> {
					return Language.getInstance().getOrDefault(
							"jade.ignore_list.comment",
							"This is an ignore list for the target of Jade. You can add registry ids to the \"values\" list.");
				}),
				Codec.STRING.listOf().fieldOf("values").forGetter($ -> $.values),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("version", 1).forGetter($ -> $.version)
		).apply(i, (comment, values, version) -> {
			IgnoreList<T> ignoreList = new IgnoreList<>();
			ignoreList.values = values;
			ignoreList.version = version;
			return ignoreList;
		}));
	}

	public static Codec<int[]> intArrayCodec(int size, Codec<Integer> codec) {
		return Codec.list(codec).flatXmap($ -> {
			if ($.size() != size) {
				return DataResult.error(() -> "Expected array of length " + size + ", got " + $.size());
			}
			int[] array = new int[size];
			for (int i = 0; i < array.length; i++) {
				array[i] = $.get(i);
			}
			return DataResult.success(array);
		}, $ -> {
			if ($.length != size) {
				return DataResult.error(() -> "Expected array of length " + size + ", got " + $.length);
			}
			IntList list = new IntArrayList(size);
			for (int i : $) {
				list.add(i);
			}
			return DataResult.success(list);
		});
	}

	public static Codec<float[]> floatArrayCodec(int size, Codec<Float> codec) {
		return Codec.list(codec).flatXmap($ -> {
			if ($.size() != size) {
				return DataResult.error(() -> "Expected array of length " + size + ", got " + $.size());
			}
			float[] array = new float[size];
			for (int i = 0; i < array.length; i++) {
				array[i] = $.get(i);
			}
			return DataResult.success(array);
		}, $ -> {
			if ($.length != size) {
				return DataResult.error(() -> "Expected array of length " + size + ", got " + $.length);
			}
			FloatList list = new FloatArrayList(size);
			for (float f : $) {
				list.add(f);
			}
			return DataResult.success(list);
		});
	}

	public static Optional<int[]> nullableClone(int[] array) {
		if (array == null) {
			return Optional.empty();
		}
		return Optional.of(array.clone());
	}

	public static Optional<float[]> nullableClone(float[] array) {
		if (array == null) {
			return Optional.empty();
		}
		return Optional.of(array.clone());
	}

	public static <T> T createFromEmptyMap(Codec<T> codec) {
		return codec.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
	}
}
