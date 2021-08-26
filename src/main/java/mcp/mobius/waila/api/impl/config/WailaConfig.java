package mcp.mobius.waila.api.impl.config;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext.FluidMode;

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
		private boolean hideFromDebug = true;
		private IconMode iconMode = IconMode.TOP;
		private boolean enableTextToSpeech = false;
		private int maxHealthForRender = 40;
		private int maxHeartsPerLine = 10;
		private FluidMode fluldMode = FluidMode.NONE;
		private float reachDistance = 0;

		public void setDisplayTooltip(boolean displayTooltip) {
			this.displayTooltip = displayTooltip;
		}

		public void setShiftForDetails(boolean shiftForDetails) {
			this.shiftForDetails = shiftForDetails;
		}

		public void setDisplayMode(DisplayMode displayMode) {
			this.displayMode = displayMode;
		}

		public void setHideFromDebug(boolean hideFromDebug) {
			this.hideFromDebug = hideFromDebug;
		}

		public void setIconMode(IconMode iconMode) {
			this.iconMode = iconMode;
		}

		public IconMode getIconMode() {
			return iconMode;
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

		public void setDisplayFluids(FluidMode displayFluids) {
			this.fluldMode = displayFluids;
		}

		public void setDisplayFluids(boolean displayFluids) {
			this.fluldMode = displayFluids ? FluidMode.ANY : FluidMode.NONE;
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

		public boolean shouldHideFromDebug() {
			return hideFromDebug;
		}

		public boolean shouldShowItem() {
			return iconMode != IconMode.HIDE;
		}

		public boolean shouldEnableTextToSpeech() {
			return enableTextToSpeech;
		}

		public int getMaxHealthForRender() {
			return maxHealthForRender;
		}

		public int getMaxHeartsPerLine() {
			return Math.max(1, maxHeartsPerLine);
		}

		public boolean shouldDisplayFluids() {
			return fluldMode != FluidMode.NONE;
		}

		public FluidMode getDisplayFluids() {
			return fluldMode;
		}

		public float getReachDistance() {
			return reachDistance;
		}

		public void setReachDistance(float reachDistance) {
			this.reachDistance = MathHelper.clamp(reachDistance, 0, 20);
		}
	}

	public static class ConfigOverlay {
		private float overlayPosX = 0.5F;
		private float overlayPosY = 0.99F;
		private float overlayAnchorX = 0.5F;
		private float overlayAnchorY = 0F;
		private boolean overlaySquare = false;
		private boolean flipMainHand = false;
		private ConfigOverlayColor color = new ConfigOverlayColor();

		public void setOverlayPosX(float overlayPosX) {
			this.overlayPosX = MathHelper.clamp(overlayPosX, 0.0F, 1.0F);
		}

		public void setOverlayPosY(float overlayPosY) {
			this.overlayPosY = MathHelper.clamp(overlayPosY, 0.0F, 1.0F);
		}

		public void setOverlayScale(float overlayScale) {
		}

		public void setAnchorX(float overlayAnchorX) {
			this.overlayAnchorX = MathHelper.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		public void setAnchorY(float overlayAnchorY) {
			this.overlayAnchorY = MathHelper.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		public float getOverlayPosX() {
			return MathHelper.clamp(overlayPosX, 0.0F, 1.0F);
		}

		public float getOverlayPosY() {
			return MathHelper.clamp(overlayPosY, 0.0F, 1.0F);
		}

		public float getOverlayScale() {
			return 1;
		}

		public float getAnchorX() {
			return MathHelper.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		public float getAnchorY() {
			return MathHelper.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		public void setFlipMainHand(boolean overlaySquare) {
			this.flipMainHand = overlaySquare;
		}

		public boolean getFlipMainHand() {
			return flipMainHand;
		}

		public float tryFlip(float f) {
			if (Minecraft.getInstance().gameSettings.mainHand == HandSide.LEFT)
				f = 1 - f;
			return f;
		}

		public void setSquare(boolean overlaySquare) {
			this.overlaySquare = overlaySquare;
		}

		public boolean getSquare() {
			return overlaySquare;
		}

		public ConfigOverlayColor getColor() {
			return color;
		}

		public static class ConfigOverlayColor {
			private int alpha = 50;
			private Map<ResourceLocation, HUDTheme> themes = Maps.newHashMap();
			private ResourceLocation activeTheme = HUDTheme.DARK.getId();

			public ConfigOverlayColor() {
				themes.put(HUDTheme.VANILLA.getId(), HUDTheme.VANILLA);
				themes.put(HUDTheme.DARK.getId(), HUDTheme.DARK);
			}

			public int getAlpha() {
				return MathHelper.clamp((int) (alpha / 100.0F * 256), 0, 255) << 24;
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
		HOLD_KEY, TOGGLE
	}

	public enum IconMode {
		TOP, CENTERED, HIDE;
	}
}
