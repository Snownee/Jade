package snownee.jade.api.theme;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.JadeIds;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IBoxElement;

public class Theme {

	public static final ResourceLocation DEFAULT_THEME_ID = JadeIds.JADE("dark");
	public ResourceLocation id;
	public String styleName;
	public BoxStyle tooltipStyle;
	public BoxStyle nestedBoxStyle;
	public BoxStyle viewGroupStyle;
	public TextSetting text;
	public float changeOpacity;
	public boolean lightColorScheme;
	public ResourceLocation iconSlotSprite;
	public int iconSlotInflation;
	public IBoxElement iconSlotSpriteCache;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public Theme(
			String styleName,
			BoxStyle tooltipStyle,
			BoxStyle nestedBoxStyle,
			BoxStyle viewGroupStyle,
			TextSetting text,
			float changeOpacity,
			boolean lightColorScheme,
			Optional<ResourceLocation> iconSlotSprite,
			int iconSlotInflation) {
		this.styleName = styleName;
		this.tooltipStyle = tooltipStyle;
		this.nestedBoxStyle = nestedBoxStyle;
		this.viewGroupStyle = viewGroupStyle;
		this.text = text;
		this.changeOpacity = changeOpacity;
		this.lightColorScheme = lightColorScheme;
		this.iconSlotSprite = iconSlotSprite.orElse(null);
		this.iconSlotInflation = iconSlotInflation;
	}

	public ResourceLocation mainId() {
		if (id.getPath().contains("/")) {
			return id.withPath(id.getPath().substring(0, id.getPath().indexOf('/')));
		} else {
			return id;
		}
	}

	public String styleId() {
		if (id.getPath().contains("/")) {
			return id.getPath().substring(id.getPath().indexOf('/') + 1);
		} else {
			return "";
		}
	}
}
