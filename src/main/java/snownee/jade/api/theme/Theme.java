package snownee.jade.api.theme;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.JadeIds;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IBoxElement;

public class Theme {

	public static final ResourceLocation DEFAULT_THEME_ID = JadeIds.JADE("dark");
	public BoxStyle tooltipStyle;
	public BoxStyle nestedBoxStyle;
	public BoxStyle viewGroupStyle;
	public ResourceLocation id;
	public TextSetting text;
	public Boolean changeRoundCorner;
	public float changeOpacity;
	public boolean lightColorScheme;
	public boolean hidden;
	public ResourceLocation iconSlotSprite;
	public int iconSlotInflation;
	public IBoxElement iconSlotSpriteCache;

	public Theme(
			BoxStyle tooltipStyle,
			BoxStyle nestedBoxStyle,
			BoxStyle viewGroupStyle,
			TextSetting text,
			Optional<Boolean> changeRoundCorner,
			float changeOpacity,
			boolean lightColorScheme,
			boolean hidden,
			Optional<ResourceLocation> iconSlotSprite,
			int iconSlotInflation) {
		this.tooltipStyle = tooltipStyle;
		this.nestedBoxStyle = nestedBoxStyle;
		this.viewGroupStyle = viewGroupStyle;
		this.text = text;
		this.changeRoundCorner = changeRoundCorner.orElse(null);
		this.changeOpacity = changeOpacity;
		this.lightColorScheme = lightColorScheme;
		this.hidden = hidden;
		this.iconSlotSprite = iconSlotSprite.orElse(null);
		this.iconSlotInflation = iconSlotInflation;
	}

}
