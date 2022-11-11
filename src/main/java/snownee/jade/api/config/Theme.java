package snownee.jade.api.config;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;

public class Theme {

	public static final Theme WAILA = new Theme(new ResourceLocation(Jade.MODID, "waila"), "#100010", "#5000ff", "#28007f", "#fff", "#a0a0a0", true);
	public static final Theme DARK = new Theme(new ResourceLocation(Jade.MODID, "dark"), "#131313", "#383838", "#242424", "#fff", "#a0a0a0", true);
	public static final Theme CREATE = new Theme(new ResourceLocation(Jade.MODID, "create"), "#000", "#2a2626", "#1a1717", "#fff", "#a0a0a0", true);
	public static final Theme TOP = new Theme(new ResourceLocation(Jade.MODID, "top"), "#0695", "#999f", "#999f", "#fff", "#a0a0a0", true);

	public final ResourceLocation id;
	public final String backgroundColor;
	public final String gradientStart;
	public final String gradientEnd;
	public final String stressedTextColor;
	public final String normalTextColor;
	public final boolean textShadow;

	@Internal
	public Theme(ResourceLocation id, String backgroundColor, String gradientStart, String gradientEnd, String titleColor, String textColor, boolean textShadow) {
		this.id = id;
		this.backgroundColor = backgroundColor;
		this.gradientStart = gradientStart;
		this.gradientEnd = gradientEnd;
		stressedTextColor = titleColor;
		normalTextColor = textColor;
		this.textShadow = textShadow;
	}

}
