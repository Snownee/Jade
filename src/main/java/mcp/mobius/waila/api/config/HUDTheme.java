package mcp.mobius.waila.api.config;

import net.minecraft.resources.ResourceLocation;

public class HUDTheme {

	public static final HUDTheme WAILA = new HUDTheme(new ResourceLocation("jade:waila"), 0x100010, 0x5000ff, 0x28007f, 0xFFFFFF, 0xA0A0A0, true);
	public static final HUDTheme DARK = new HUDTheme(new ResourceLocation("jade:dark"), 0x131313, 0x383838, 0x242424, 0xFFFFFF, 0xA0A0A0, true);
	public static final HUDTheme CREATE = new HUDTheme(new ResourceLocation("jade:create"), 0x000000, 0x2a2626, 0x1a1717, 0xFFFFFF, 0xA0A0A0, true);
	public static final HUDTheme TOP = new HUDTheme(new ResourceLocation("jade:top"), 0x55006699, 0xff999999, 0xff999999, 0xFFFFFF, 0xA0A0A0, true);
	//public static final HUDTheme GRAY = new HUDTheme(new ResourceLocation("jade:gray"), 0x606060, 0x101010, 0x101010, 0xFFFFFF, 0xA0A0A0, true);

	public final ResourceLocation id;
	public final int backgroundColor;
	public final int gradientStart;
	public final int gradientEnd;
	public final int titleColor;
	public final int textColor;
	public final boolean textShadow;

	public HUDTheme(ResourceLocation id, int backgroundColor, int gradientStart, int gradientEnd, int titleColor, int textColor, boolean textShadow) {
		this.id = id;
		this.backgroundColor = backgroundColor;
		this.gradientStart = gradientStart;
		this.gradientEnd = gradientEnd;
		this.titleColor = titleColor;
		this.textColor = textColor;
		this.textShadow = textShadow;
	}

}
