package mcp.mobius.waila.api.impl.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringEscapeUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class WailaConfig {

    private final ConfigGeneral general = new ConfigGeneral();
    private final ConfigOverlay overlay = new ConfigOverlay();
    private final ConfigFormatting formatting = new ConfigFormatting();

    public ConfigGeneral getGeneral() {
        return general;
    }

    public ConfigOverlay getOverlay() {
        return overlay;
    }

    public ConfigFormatting getFormatting() {
        return formatting;
    }

    public static class ConfigGeneral {
        private boolean displayTooltip = true;
        private boolean shiftForDetails = false;
        private DisplayMode displayMode = DisplayMode.TOGGLE;
        private boolean hideFromPlayerList = true;
        private boolean hideFromDebug = true;
        private boolean showItem = true;
        private boolean enableTextToSpeech = false;
        private int maxHealthForRender = 40;
        private int maxHeartsPerLine = 10;
        private boolean displayFluids;

        public void setDisplayTooltip(boolean displayTooltip) {
            this.displayTooltip = displayTooltip;
        }

        public void setShiftForDetails(boolean shiftForDetails) {
            this.shiftForDetails = shiftForDetails;
        }

        public void setDisplayMode(DisplayMode displayMode) {
            this.displayMode = displayMode;
        }

        public void setHideFromPlayerList(boolean hideFromPlayerList) {
            this.hideFromPlayerList = hideFromPlayerList;
        }

        public void setHideFromDebug(boolean hideFromDebug) {
            this.hideFromDebug = hideFromDebug;
        }

        public void setShowItem(boolean showItem) {
            this.showItem = showItem;
        }

        public void setEnableTextToSpeech(boolean enableTextToSpeech) {
            this.enableTextToSpeech = enableTextToSpeech;
        }

        public void setMaxHealthForRender(int maxHealthForRender) {
            this.maxHealthForRender = maxHealthForRender;
        }

        public void setMaxHeartsPerLine(int maxHeartsPerLine) {
            this.maxHeartsPerLine = maxHeartsPerLine;
        }

        public void setDisplayFluids(boolean displayFluids) {
            this.displayFluids = displayFluids;
        }

        public boolean shouldDisplayTooltip() {
            return displayTooltip;
        }

        public boolean shouldShiftForDetails() {
            return shiftForDetails;
        }

        public DisplayMode getDisplayMode() {
            return displayMode;
        }

        public boolean shouldHideFromPlayerList() {
            return hideFromPlayerList;
        }

        public boolean shouldHideFromDebug() {
            return hideFromDebug;
        }

        public boolean shouldShowItem() {
            return showItem;
        }

        public boolean shouldEnableTextToSpeech() {
            return enableTextToSpeech;
        }

        public int getMaxHealthForRender() {
            return maxHealthForRender;
        }

        public int getMaxHeartsPerLine() {
            return maxHeartsPerLine;
        }

        public boolean shouldDisplayFluids() {
            return displayFluids;
        }
    }

    public static class ConfigOverlay {
        private float overlayPosX = 0.5F;
        private float overlayPosY = 0.99F;
        private float overlayScale = 1.0F;
        private ConfigOverlayColor color = new ConfigOverlayColor();

        public void setOverlayPosX(float overlayPosX) {
            this.overlayPosX = overlayPosX;
        }

        public void setOverlayPosY(float overlayPosY) {
            this.overlayPosY = overlayPosY;
        }

        public void setOverlayScale(float overlayScale) {
            this.overlayScale = overlayScale;
        }

        public float getOverlayPosX() {
            return Math.min(Math.max(overlayPosX, 0.0F), 1.0F);
        }

        public float getOverlayPosY() {
            return Math.min(Math.max(overlayPosY, 0.0F), 1.0F);
        }

        public float getOverlayScale() {
            return overlayScale;
        }

        public ConfigOverlayColor getColor() {
            return color;
        }

        public static class ConfigOverlayColor {
            private int alpha = 80;
            private Map<ResourceLocation, HUDTheme> themes = Maps.newHashMap();
            private ResourceLocation activeTheme = HUDTheme.VANILLA.getId();

            public ConfigOverlayColor() {
                themes.put(HUDTheme.VANILLA.getId(), HUDTheme.VANILLA);
                themes.put(HUDTheme.DARK.getId(), HUDTheme.DARK);
            }

            public int getAlpha() {
                return alpha == 100 ? 255 : alpha == 0 ? (int) (0.4F / 100.0F * 256) << 24 : (int) (alpha / 100.0F * 256) << 24;
            }

            public int getRawAlpha() {
                return alpha;
            }

            public HUDTheme getTheme() {
                return themes.getOrDefault(activeTheme, HUDTheme.VANILLA);
            }

            public Collection<HUDTheme> getThemes() {
                return themes.values();
            }

            public void setAlpha(int alpha) {
                this.alpha = alpha;
            }

            public int getBackgroundColor() {
                return getAlpha() + getTheme().getBackgroundColor();
            }

            public int getGradientStart() {
                return getAlpha() + getTheme().getGradientStart();
            }

            public int getGradientEnd() {
                return getAlpha() + getTheme().getGradientEnd();
            }

            public int getFontColor() {
                return getTheme().getFontColor();
            }

            public void applyTheme(ResourceLocation id) {
                activeTheme = themes.containsKey(id) ? id : activeTheme;
            }

            public static class Adapter implements JsonSerializer<ConfigOverlayColor>, JsonDeserializer<ConfigOverlayColor> {
                @Override
                public ConfigOverlayColor deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject json = element.getAsJsonObject();
                    ConfigOverlayColor color = new ConfigOverlayColor();
                    color.alpha = json.getAsJsonPrimitive("alpha").getAsInt();
                    color.activeTheme = new ResourceLocation(json.getAsJsonPrimitive("activeTheme").getAsString());
                    color.themes = Maps.newHashMap();
                    json.getAsJsonArray("themes").forEach(e -> {
                        HUDTheme theme = context.deserialize(e, HUDTheme.class);
                        color.themes.put(theme.getId(), theme);
                    });
                    return color;
                }

                @Override
                public JsonElement serialize(ConfigOverlayColor src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject json = new JsonObject();
                    json.addProperty("alpha", src.alpha);
                    json.add("themes", context.serialize(src.themes.values()));
                    json.addProperty("activeTheme", src.activeTheme.toString());
                    return json;
                }
            }
        }
    }

    public static class ConfigFormatting {
        private String modName = StringEscapeUtils.escapeJava("\u00A79\u00A7o%s");
        private String blockName = StringEscapeUtils.escapeJava("\u00a7f%s");
        private String fluidName = StringEscapeUtils.escapeJava("\u00a7f%s");
        private String entityName = StringEscapeUtils.escapeJava("\u00a7f%s");
        private String registryName = StringEscapeUtils.escapeJava("\u00a77[%s]");

        public void setModName(String modName) {
            this.modName = modName;
        }

        public void setBlockName(String blockName) {
            this.blockName = blockName;
        }

        public void setFluidName(String fluidName) {
            this.fluidName = fluidName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        public void setRegistryName(String registryName) {
            this.registryName = registryName;
        }

        public String getModName() {
            return StringEscapeUtils.unescapeJava(modName);
        }

        public String getBlockName() {
            return StringEscapeUtils.unescapeJava(blockName);
        }

        public String getFluidName() {
            return StringEscapeUtils.unescapeJava(fluidName);
        }

        public String getEntityName() {
            return StringEscapeUtils.unescapeJava(entityName);
        }

        public String getRegistryName() {
            return StringEscapeUtils.unescapeJava(registryName);
        }
    }

    public enum DisplayMode {
        HOLD_KEY,
        TOGGLE
    }
}
