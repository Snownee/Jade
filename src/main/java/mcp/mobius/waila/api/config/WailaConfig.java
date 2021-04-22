package mcp.mobius.waila.api.config;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;

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
		private boolean showIcon = true;
		private boolean enableTextToSpeech = false;
		private int maxHealthForRender = 40;
		private int maxHeartsPerLine = 10;
		private FluidMode fluldMode = FluidMode.NONE;
		private float reachDistance = 0;
		@Expose
		private boolean debug = false;

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

		public void setShowIcon(boolean showIcon) {
			this.showIcon = showIcon;
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

		public boolean shouldShowIcon() {
			return showIcon;
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
			return fluldMode != FluidMode.NONE;
		}

		public FluidMode getDisplayFluids() {
			return fluldMode;
		}

		public float getReachDistance() {
			return reachDistance;
		}

		public void setReachDistance(float reachDistance) {
			this.reachDistance = MathHelper.clamp(reachDistance, 0, 30);
		}

		public void setDebug(boolean debug) {
			this.debug = debug;
		}

		public boolean isDebug() {
			return debug;
		}
	}

	public static class ConfigOverlay {
		private float overlayPosX = 0.5F;
		private float overlayPosY = 0.99F;
		private float overlayScale = 1.0F;
		private float overlayAnchorX = 0.5F;
		private float overlayAnchorY = 0F;
		private boolean overlaySquare = false;
		private boolean flipMainHand = false;
		private ConfigOverlayColor color = new ConfigOverlayColor();
		@Expose
		private float autoScaleThreshold = 0.5f;

		public void setOverlayPosX(float overlayPosX) {
			this.overlayPosX = MathHelper.clamp(overlayPosX, 0.0F, 1.0F);
		}

		public void setOverlayPosY(float overlayPosY) {
			this.overlayPosY = MathHelper.clamp(overlayPosY, 0.0F, 1.0F);
		}

		public void setOverlayScale(float overlayScale) {
			this.overlayScale = MathHelper.clamp(overlayScale, 0.2F, 2.0F);
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
			return overlayScale;
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

		public float getAutoScaleThreshold() {
			return autoScaleThreshold;
		}

		public ConfigOverlayColor getColor() {
			return color;
		}

		public static class ConfigOverlayColor {
			private int alpha = 70;
			private Map<ResourceLocation, HUDTheme> themes = Maps.newHashMap();
			private ResourceLocation activeTheme = HUDTheme.DARK.getId();

			public ConfigOverlayColor() {
				themes.put(HUDTheme.WAILA.getId(), HUDTheme.WAILA);
				themes.put(HUDTheme.DARK.getId(), HUDTheme.DARK);
				themes.put(HUDTheme.CREATE.getId(), HUDTheme.CREATE);
				themes.put(HUDTheme.TOP.getId(), HUDTheme.TOP);
			}

			public float getAlpha() {
				return MathHelper.clamp(alpha / 100f, 0, 1);
			}

			public int getRawAlpha() {
				return alpha;
			}

			public HUDTheme getTheme() {
				return themes.getOrDefault(activeTheme, HUDTheme.DARK);
			}

			public Collection<HUDTheme> getThemes() {
				return themes.values();
			}

			public void setAlpha(int alpha) {
				this.alpha = alpha;
			}

			public int getBackgroundColor() {
				return applyAlpha(getTheme().getBackgroundColor(), getAlpha());
			}

			public int getGradientStart() {
				return applyAlpha(getTheme().getGradientStart(), getAlpha());
			}

			public int getGradientEnd() {
				return applyAlpha(getTheme().getGradientEnd(), getAlpha());
			}

			public int getFontColor() {
				return getTheme().getFontColor();
			}

			public static int applyAlpha(int color, float alpha) {
				int prevAlphaChannel = (color >> 24) & 0xFF;
				if (prevAlphaChannel > 0)
					alpha *= prevAlphaChannel / 256f;
				int alphaChannel = (int) (0xFF * MathHelper.clamp(alpha, 0, 1));
				return (color & 0xFFFFFF) | alphaChannel << 24;
			}

			public void applyTheme(ResourceLocation id) {
				activeTheme = themes.containsKey(id) ? id : activeTheme;
			}
		}
	}

	public static class ConfigFormatting {
		private String modName = "§9§o%s";
		private String blockName = "§f%s";
		private String fluidName = "§f%s";
		private String entityName = "§f%s";
		private String registryName = "§7[%s]";

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
			return modName;
		}

		public String getBlockName() {
			return blockName;
		}

		public String getFluidName() {
			return fluidName;
		}

		public String getEntityName() {
			return entityName;
		}

		public String getRegistryName() {
			return registryName;
		}
	}

	public enum DisplayMode {
		HOLD_KEY, TOGGLE
	}
}
