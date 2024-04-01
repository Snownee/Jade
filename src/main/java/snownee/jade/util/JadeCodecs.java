package snownee.jade.util;

import java.util.Optional;
import java.util.OptionalInt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import snownee.jade.api.config.IgnoreList;
import snownee.jade.api.theme.TextSetting;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.ColorPalette;

public class JadeCodecs {

	public static final Codec<TextSetting> TEXT_SETTING = RecordCodecBuilder.create(i -> i.group(
			ColorPalette.CODEC.optionalFieldOf("colors", ColorPalette.DEFAULT).forGetter(TextSetting::colors),
			Codec.BOOL.optionalFieldOf("shadow", true).forGetter(TextSetting::shadow),
			Style.Serializer.CODEC.optionalFieldOf("modNameStyle").forGetter($ -> Optional.ofNullable($.modNameStyle())),
			Color.CODEC.optionalFieldOf("itemAmountColor", 0xFFFFFFFF).forGetter(TextSetting::itemAmountColor)
	).apply(i, TextSetting::new));

	public static final Codec<Theme> THEME = RecordCodecBuilder.create(i -> i.group(
			BoxStyle.CODEC.fieldOf("tooltipStyle").forGetter($ -> $.tooltipStyle),
			BoxStyle.CODEC.optionalFieldOf("nestedBoxStyle", BoxStyle.GradientBorder.DEFAULT_NESTED_BOX).forGetter($ -> $.nestedBoxStyle),
			BoxStyle.CODEC.optionalFieldOf("viewGroupStyle", BoxStyle.GradientBorder.DEFAULT_VIEW_GROUP).forGetter($ -> $.viewGroupStyle),
			TEXT_SETTING.optionalFieldOf("text", TextSetting.DEFAULT).forGetter($ -> $.text),
			Codec.BOOL.optionalFieldOf("changeRoundCorner").forGetter($ -> Optional.ofNullable($.changeRoundCorner)),
			Codec.floatRange(0, 1).optionalFieldOf("changeOpacity", 0F).forGetter($ -> $.changeOpacity),
			Codec.BOOL.optionalFieldOf("lightColorScheme", false).forGetter($ -> $.lightColorScheme),
			Codec.BOOL.optionalFieldOf("hidden", false).forGetter($ -> $.hidden),
			ResourceLocation.CODEC.optionalFieldOf("iconSlotSprite").forGetter($ -> Optional.ofNullable($.iconSlotSprite)),
			Codec.INT.optionalFieldOf("iconSlotInflation", 0).forGetter($ -> $.iconSlotInflation)
	).apply(i, Theme::new));

	public static final Codec<OptionalInt> OPTIONAL_INT = new Codec<>() {
		@Override
		public <T> DataResult<Pair<OptionalInt, T>> decode(DynamicOps<T> ops, T input) {
			return DataResult.success(ops.getNumberValue(input)
					.result()
					.map(number -> Pair.of(OptionalInt.of(number.intValue()), ops.empty()))
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

	public static <T> Codec<IgnoreList<T>> ignoreList(ResourceKey<? extends Registry<T>> registryKey) {
		return RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.optionalFieldOf("__comment", "").forGetter($ -> {
					return Language.getInstance().getOrDefault(
							"jade.ignore_list.comment",
							"This is an ignore list for the target of Jade. You can add registry ids to the \"values\" list.");
				}),
				ResourceKey.codec(registryKey).listOf().fieldOf("values").forGetter($ -> $.values),
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
		return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()), IllegalStateException::new);
	}
}
