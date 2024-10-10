package snownee.jade.util;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.theme.TextSetting;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.ColorPalette;

public class JadeClientCodecs {
	public static final Codec<TextSetting> TEXT_SETTING = RecordCodecBuilder.create(i -> i.group(
			ColorPalette.CODEC.optionalFieldOf("colors", ColorPalette.DEFAULT).forGetter(TextSetting::colors),
			Codec.BOOL.optionalFieldOf("shadow", true).forGetter(TextSetting::shadow),
			Style.Serializer.CODEC.optionalFieldOf("modNameStyle").forGetter($ -> Optional.ofNullable($.modNameStyle())),
			Color.CODEC.optionalFieldOf("itemAmountColor", 0xFFFFFFFF).forGetter(TextSetting::itemAmountColor)
	).apply(i, TextSetting::new));

	public static final MapCodec<Theme> THEME = RecordCodecBuilder.mapCodec(i -> i.group(
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

	public record ThemeHolder(int version, boolean autoEnable, Theme theme) {}
}
