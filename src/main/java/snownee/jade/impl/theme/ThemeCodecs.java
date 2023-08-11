package snownee.jade.impl.theme;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.chat.Style;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.ColorPalette;

public class ThemeCodecs {

	public static final Codec<Theme> CODEC = RecordCodecBuilder.create(i -> i.group(
			BoxStyle.CODEC.fieldOf("tooltipStyle").forGetter($ -> $.tooltipStyle),
			BoxStyle.CODEC.optionalFieldOf("nestedBoxStyle", BoxStyle.GradientBorder.DEFAULT_NESTED_BOX).forGetter($ -> $.nestedBoxStyle),
			BoxStyle.CODEC.optionalFieldOf("viewGroupStyle", BoxStyle.GradientBorder.DEFAULT_VIEW_GROUP).forGetter($ -> $.viewGroupStyle),
			ColorPalette.CODEC.optionalFieldOf("textColors", ColorPalette.DEFAULT).forGetter($ -> $.textColors),
			Codec.BOOL.optionalFieldOf("textShadow", true).forGetter($ -> $.textShadow),
			Codec.BOOL.optionalFieldOf("changeRoundCorner").forGetter($ -> Optional.ofNullable($.changeRoundCorner)),
			Codec.floatRange(0, 1).optionalFieldOf("changeOpacity", 0F).forGetter($ -> $.changeOpacity),
			Codec.BOOL.optionalFieldOf("lightColorScheme", false).forGetter($ -> $.lightColorScheme),
			Codec.BOOL.optionalFieldOf("hidden", false).forGetter($ -> $.hidden),
			Color.CODEC.optionalFieldOf("itemAmountColor", 0xFFFFFFFF).forGetter($ -> $.itemAmountColor),
			Style.FORMATTING_CODEC.optionalFieldOf("modNameStyle").forGetter($ -> Optional.ofNullable($.modNameStyle))
	).apply(i, Theme::new));

/*	@SuppressWarnings("unchecked")
	public static <T> Codec<T[]> arrayCodec(int size, Codec<T> codec) {
		return Codec.list(codec).flatXmap($ -> {
			if ($.size() != size) {
				return DataResult.error(() -> "Expected array of length " + size + ", got " + $.size());
			}
			return DataResult.success((T[]) $.toArray());
		}, $ -> {
			if ($.length != size) {
				return DataResult.error(() -> "Expected array of length " + size + ", got " + $.length);
			}
			return DataResult.success(List.of($));
		});
	}*/

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
		if (array == null)
			return Optional.empty();
		return Optional.of(array.clone());
	}

	public static Optional<float[]> nullableClone(float[] array) {
		if (array == null)
			return Optional.empty();
		return Optional.of(array.clone());
	}

}
