package mcp.mobius.waila.api.impl.config;

import mcp.mobius.waila.Waila;
import net.minecraft.util.ResourceLocation;

public class HUDTheme {

    public static final HUDTheme VANILLA = new HUDTheme();
    public static final HUDTheme DARK = new HUDTheme(new ResourceLocation(Waila.MODID, "dark"), 0x131313, 0x383838, 0x242424, 0xA0A0A0);

    private final ResourceLocation id;
    private final int backgroundColor;
    private final int gradientStart;
    private final int gradientEnd;
    private final int fontColor;

    public HUDTheme(ResourceLocation id, int backgroundColor, int gradientStart, int gradientEnd, int fontColor) {
        this.id = id;
        this.backgroundColor = backgroundColor;
        this.gradientStart = gradientStart;
        this.gradientEnd = gradientEnd;
        this.fontColor = fontColor;
    }

    public HUDTheme() {
        this(new ResourceLocation(Waila.MODID, "vanilla"), 0x100010, 0x5000ff, 0x28007f, 0xA0A0A0);
    }

    public int getAlpha() {
        int alpha = Waila.CONFIG.get().getOverlay().getColor().getAlpha();
        return alpha == 100 ? 255 : alpha == 0 ? (int) (0.4F / 100.0F * 256) << 24 : (int) (alpha / 100.0F * 256) << 24;
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getGradientStart() {
        return gradientStart;
    }

    public int getGradientEnd() {
        return gradientEnd;
    }

    public int getFontColor() {
        return fontColor;
    }
}
