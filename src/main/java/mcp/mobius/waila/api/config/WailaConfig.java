package mcp.mobius.waila.api.config;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.ClipContext;

/**
 * Get this instance from {@link mcp.mobius.waila.api.IWailaCommonRegistration#getConfig}
 */
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
		private boolean displayBlocks = true;
		private boolean displayEntities = true;
		private DisplayMode displayMode = DisplayMode.TOGGLE;
		private boolean hideFromDebug = true;
		private IconMode iconMode = IconMode.TOP;
		private boolean enableTextToSpeech = false;
		private TTSMode ttsMode = TTSMode.PRESS;
		private int maxHealthForRender = 40;
		private int maxHeartsPerLine = 10;
		private FluidMode fluidMode = FluidMode.NONE;
		private float reachDistance = 0;
		@Expose
		private boolean debug = false;
		public boolean hintOverlayToggle = true;

		public void setDisplayTooltip(boolean displayTooltip) {
			this.displayTooltip = displayTooltip;
		}

		public boolean getDisplayEntities() {
			return displayEntities;
		}

		public boolean getDisplayBlocks() {
			return displayBlocks;
		}

		public void setDisplayBlocks(boolean displayBlocks) {
			this.displayBlocks = displayBlocks;
		}

		public void setDisplayEntities(boolean displayEntities) {
			this.displayEntities = displayEntities;
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

		public void toggleTTS() {
			enableTextToSpeech = !enableTextToSpeech;
		}

		public void setTTSMode(TTSMode ttsMode) {
			this.ttsMode = ttsMode;
		}

		public void setMaxHealthForRender(int maxHealthForRender) {
			this.maxHealthForRender = maxHealthForRender;
		}

		public void setMaxHeartsPerLine(int maxHeartsPerLine) {
			this.maxHeartsPerLine = maxHeartsPerLine;
		}

		public void setDisplayFluids(boolean displayFluids) {
			fluidMode = displayFluids ? FluidMode.ANY : FluidMode.NONE;
		}

		public void setDisplayFluids(FluidMode displayFluids) {
			fluidMode = displayFluids;
		}

		public boolean shouldDisplayTooltip() {
			return displayTooltip;
		}

		public DisplayMode getDisplayMode() {
			return displayMode;
		}

		public boolean shouldHideFromDebug() {
			return hideFromDebug;
		}

		public IconMode getIconMode() {
			return iconMode;
		}

		public boolean shouldShowIcon() {
			return iconMode != IconMode.HIDE;
		}

		public boolean shouldEnableTextToSpeech() {
			return ttsMode == TTSMode.TOGGLE && enableTextToSpeech;
		}

		public TTSMode getTTSMode() {
			return ttsMode;
		}

		public int getMaxHealthForRender() {
			return maxHealthForRender;
		}

		public int getMaxHeartsPerLine() {
			return Math.max(1, maxHeartsPerLine);
		}

		public boolean shouldDisplayFluids() {
			return fluidMode != FluidMode.NONE;
		}

		public FluidMode getDisplayFluids() {
			return fluidMode;
		}

		public float getReachDistance() {
			return reachDistance;
		}

		public void setReachDistance(float reachDistance) {
			this.reachDistance = Mth.clamp(reachDistance, 0, 20);
		}

		public void setDebug(boolean debug) {
			this.debug = debug;
		}

		public boolean isDebug() {
			return debug;
		}

		public enum IconMode {
			TOP, CENTERED, HIDE;
		}

		public enum TTSMode {
			TOGGLE, PRESS
		}
	}

	public static class ConfigOverlay {
		private float overlayPosX = 0.5F;
		private float overlayPosY = 1.0F;
		private float overlayScale = 1.0F;
		private float overlayAnchorX = 0.5F;
		private float overlayAnchorY = 0F;
		private boolean overlaySquare = false;
		private boolean flipMainHand = false;
		private ConfigOverlayColor color = new ConfigOverlayColor();
		@Expose
		private float autoScaleThreshold = 0.5f;

		public void setOverlayPosX(float overlayPosX) {
			this.overlayPosX = Mth.clamp(overlayPosX, 0.0F, 1.0F);
		}

		public void setOverlayPosY(float overlayPosY) {
			this.overlayPosY = Mth.clamp(overlayPosY, 0.0F, 1.0F);
		}

		public void setOverlayScale(float overlayScale) {
			this.overlayScale = Mth.clamp(overlayScale, 0.2F, 2.0F);
		}

		public void setAnchorX(float overlayAnchorX) {
			this.overlayAnchorX = Mth.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		public void setAnchorY(float overlayAnchorY) {
			this.overlayAnchorY = Mth.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		public float getOverlayPosX() {
			return Mth.clamp(overlayPosX, 0.0F, 1.0F);
		}

		public float getOverlayPosY() {
			return Mth.clamp(overlayPosY, 0.0F, 1.0F);
		}

		public float getOverlayScale() {
			return overlayScale;
		}

		public float getAnchorX() {
			return Mth.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		public float getAnchorY() {
			return Mth.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		public void setFlipMainHand(boolean overlaySquare) {
			flipMainHand = overlaySquare;
		}

		public boolean getFlipMainHand() {
			return flipMainHand;
		}

		public float tryFlip(float f) {
			if (Minecraft.getInstance().options.mainHand == HumanoidArm.LEFT)
				f = 1 - f;
			return f;
		}

		public void setSquare(boolean overlaySquare) {
			this.overlaySquare = overlaySquare;
		}

		public boolean getSquare() {
			return overlaySquare;
		}

		public float getAutoScaleThreshold() {
			return autoScaleThreshold;
		}

		public ConfigOverlayColor getColor() {
			return color;
		}

		public static class ConfigOverlayColor {
			private float alpha = 0.7f;
			private Map<ResourceLocation, HUDTheme> themes = Maps.newLinkedHashMap();
			private ResourceLocation activeTheme = HUDTheme.DARK.id;

			public ConfigOverlayColor() {
				themes.put(HUDTheme.WAILA.id, HUDTheme.WAILA);
				themes.put(HUDTheme.DARK.id, HUDTheme.DARK);
				themes.put(HUDTheme.CREATE.id, HUDTheme.CREATE);
				themes.put(HUDTheme.TOP.id, HUDTheme.TOP);
				//themes.put(HUDTheme.GRAY.id, HUDTheme.GRAY);
			}

			public float getAlpha() {
				return alpha;
			}

			public HUDTheme getTheme() {
				return themes.getOrDefault(activeTheme, HUDTheme.DARK);
			}

			public Collection<HUDTheme> getThemes() {
				return themes.values();
			}

			public void setAlpha(float alpha) {
				this.alpha = Mth.clamp(alpha, 0, 1);
			}

			public int getBackgroundColor() {
				return applyAlpha(getTheme().backgroundColor, getAlpha());
			}

			public int getGradientStart() {
				return applyAlpha(getTheme().gradientStart, getAlpha());
			}

			public int getGradientEnd() {
				return applyAlpha(getTheme().gradientEnd, getAlpha());
			}

			public static int applyAlpha(int color, float alpha) {
				int prevAlphaChannel = (color >> 24) & 0xFF;
				if (prevAlphaChannel > 0)
					alpha *= prevAlphaChannel / 256f;
				int alphaChannel = (int) (0xFF * Mth.clamp(alpha, 0, 1));
				return (color & 0xFFFFFF) | alphaChannel << 24;
			}

			public void applyTheme(ResourceLocation id) {
				activeTheme = themes.containsKey(id) ? id : activeTheme;
			}

			public Style getTitle() {
				return color(getTheme().titleColor);
			}

			private static Style color(int color) {
				return Style.EMPTY.withColor(color);
			}
		}
	}

	public static class ConfigFormatting {
		private String modName = "§9§o%s";
		private String blockName = "%s";
		private String entityName = "%s";
		private String registryName = "§7[%s]";

		public void setModName(String modName) {
			this.modName = modName;
		}

		public void setBlockName(String blockName) {
			this.blockName = blockName;
		}

		public void setEntityName(String entityName) {
			this.entityName = entityName;
		}

		public void setRegistryName(String registryName) {
			this.registryName = registryName;
		}

		public String getModName() {
			return modName;
		}

		public String getBlockName() {
			return blockName;
		}

		public String getEntityName() {
			return entityName;
		}

		public String getRegistryName() {
			return registryName;
		}
	}

	public enum DisplayMode {
		HOLD_KEY, TOGGLE, LITE
	}

	public enum FluidMode {
		NONE(ClipContext.Fluid.NONE), ANY(ClipContext.Fluid.ANY), SOURCE_ONLY(ClipContext.Fluid.SOURCE_ONLY);

		public final ClipContext.Fluid ctx;

		FluidMode(ClipContext.Fluid ctx) {
			this.ctx = ctx;
		}
	}
}
