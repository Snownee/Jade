package snownee.jade.api.theme;

import java.util.Optional;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.ColorPalette;

public class Theme {

	public static final ResourceLocation DEFAULT_THEME_ID = Identifiers.JADE("dark");
	public BoxStyle tooltipStyle;
	public BoxStyle nestedBoxStyle;
	public BoxStyle viewGroupStyle;
	public ResourceLocation id;
	public ColorPalette textColors;
	public int itemAmountColor;
	public boolean textShadow;
	public Boolean changeRoundCorner;
	public float changeOpacity;
	public boolean lightColorScheme;
	public boolean hidden;
	public Style modNameStyle;

	public Theme(BoxStyle tooltipStyle, BoxStyle nestedBoxStyle, BoxStyle viewGroupStyle, ColorPalette textColors, boolean textShadow, Optional<Boolean> changeRoundCorner, float changeOpacity, boolean lightColorScheme, boolean hidden, int itemAmountColor, Optional<Style> modNameStyle) {
		this.tooltipStyle = tooltipStyle;
		this.nestedBoxStyle = nestedBoxStyle;
		this.viewGroupStyle = viewGroupStyle;
		this.textColors = textColors;
		this.textShadow = textShadow;
		this.changeRoundCorner = changeRoundCorner.orElse(null);
		this.changeOpacity = changeOpacity;
		this.lightColorScheme = lightColorScheme;
		this.hidden = hidden;
		this.itemAmountColor = itemAmountColor;
		this.modNameStyle = modNameStyle.orElse(null);
	}

}
