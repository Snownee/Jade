package snownee.jade.impl.config;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.Theme;
import snownee.jade.overlay.OverlayRenderer;

/**
 * Get this instance from {@link mcp.IWailaCommonRegistration.waila.api.IWailaCommonRegistration#getConfig}
 */
public class WailaConfig implements IWailaConfig {

	private final ConfigGeneral general = new ConfigGeneral();
	private final ConfigOverlay overlay = new ConfigOverlay();
	private final ConfigFormatting formatting = new ConfigFormatting();

	@Override
	public ConfigGeneral getGeneral() {
		return general;
	}

	@Override
	public ConfigOverlay getOverlay() {
		return overlay;
	}

	@Override
	public ConfigFormatting getFormatting() {
		return formatting;
	}

	public static class ConfigGeneral implements IConfigGeneral {
		private boolean displayTooltip = true;
		private boolean displayBlocks = true;
		private boolean displayEntities = true;
		private DisplayMode displayMode = DisplayMode.TOGGLE;
		private boolean hideFromDebug = true;
		private boolean enableTextToSpeech = false;
		private TTSMode ttsMode = TTSMode.PRESS;
		private int maxHealthForRender = 40;
		private int maxHeartsPerLine = 10;
		private FluidMode fluidMode = FluidMode.ANY;
		private float reachDistance = 0;
		@Expose
		private boolean debug = false;
		private boolean itemModNameTooltip = true;

		@Override
		public void setDisplayTooltip(boolean displayTooltip) {
			this.displayTooltip = displayTooltip;
		}

		@Override
		public boolean getDisplayEntities() {
			return displayEntities;
		}

		@Override
		public boolean getDisplayBlocks() {
			return displayBlocks;
		}

		@Override
		public void setDisplayBlocks(boolean displayBlocks) {
			this.displayBlocks = displayBlocks;
		}

		@Override
		public void setDisplayEntities(boolean displayEntities) {
			this.displayEntities = displayEntities;
		}

		@Override
		public void setDisplayMode(DisplayMode displayMode) {
			this.displayMode = displayMode;
		}

		@Override
		public void setHideFromDebug(boolean hideFromDebug) {
			this.hideFromDebug = hideFromDebug;
		}

		@Override
		public void toggleTTS() {
			enableTextToSpeech = !enableTextToSpeech;
		}

		@Override
		public void setTTSMode(TTSMode ttsMode) {
			this.ttsMode = ttsMode;
		}

		@Override
		public void setMaxHealthForRender(int maxHealthForRender) {
			this.maxHealthForRender = maxHealthForRender;
		}

		@Override
		public void setMaxHeartsPerLine(int maxHeartsPerLine) {
			this.maxHeartsPerLine = maxHeartsPerLine;
		}

		@Override
		public void setDisplayFluids(boolean displayFluids) {
			fluidMode = displayFluids ? FluidMode.ANY : FluidMode.NONE;
		}

		@Override
		public void setDisplayFluids(FluidMode displayFluids) {
			fluidMode = displayFluids;
		}

		@Override
		public boolean shouldDisplayTooltip() {
			return displayTooltip;
		}

		@Override
		public DisplayMode getDisplayMode() {
			return displayMode;
		}

		@Override
		public boolean shouldHideFromDebug() {
			return hideFromDebug;
		}

		@Override
		public boolean shouldEnableTextToSpeech() {
			return ttsMode == TTSMode.TOGGLE && enableTextToSpeech;
		}

		@Override
		public TTSMode getTTSMode() {
			return ttsMode;
		}

		@Override
		public int getMaxHealthForRender() {
			return maxHealthForRender;
		}

		@Override
		public int getMaxHeartsPerLine() {
			return Math.max(1, maxHeartsPerLine);
		}

		@Override
		public boolean shouldDisplayFluids() {
			return fluidMode != FluidMode.NONE;
		}

		@Override
		public FluidMode getDisplayFluids() {
			return fluidMode;
		}

		@Override
		public float getReachDistance() {
			return reachDistance;
		}

		@Override
		public void setReachDistance(float reachDistance) {
			this.reachDistance = Mth.clamp(reachDistance, 0, 20);
		}

		@Override
		public void setDebug(boolean debug) {
			this.debug = debug;
		}

		@Override
		public boolean isDebug() {
			return debug;
		}

		@Override
		public void setItemModNameTooltip(boolean itemModNameTooltip) {
			this.itemModNameTooltip = itemModNameTooltip;
		}

		@Override
		public boolean showItemModNameTooltip() {
			return itemModNameTooltip;
		}

	}

	public static class ConfigOverlay implements IConfigOverlay {
		private float overlayPosX = 0.5F;
		private float overlayPosY = 1.0F;
		private float overlayScale = 1.0F;
		private float overlayAnchorX = 0.5F;
		private float overlayAnchorY = 0F;
		private boolean overlaySquare = false;
		private boolean flipMainHand = false;
		@Expose
		private float autoScaleThreshold = 0.5f;
		private float alpha = 0.7f;
		private Map<ResourceLocation, Theme> themes = Maps.newLinkedHashMap();
		private ResourceLocation activeTheme = Theme.DARK.id;
		private IconMode iconMode = IconMode.TOP;

		public ConfigOverlay() {
			themes.put(Theme.WAILA.id, Theme.WAILA);
			themes.put(Theme.DARK.id, Theme.DARK);
			themes.put(Theme.CREATE.id, Theme.CREATE);
			themes.put(Theme.TOP.id, Theme.TOP);
			//themes.put(HUDTheme.GRAY.id, HUDTheme.GRAY);
		}

		@Override
		public void setOverlayPosX(float overlayPosX) {
			this.overlayPosX = Mth.clamp(overlayPosX, 0.0F, 1.0F);
		}

		@Override
		public void setOverlayPosY(float overlayPosY) {
			this.overlayPosY = Mth.clamp(overlayPosY, 0.0F, 1.0F);
		}

		@Override
		public void setOverlayScale(float overlayScale) {
			this.overlayScale = Mth.clamp(overlayScale, 0.2F, 2.0F);
		}

		@Override
		public void setAnchorX(float overlayAnchorX) {
			this.overlayAnchorX = Mth.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		@Override
		public void setAnchorY(float overlayAnchorY) {
			this.overlayAnchorY = Mth.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		@Override
		public float getOverlayPosX() {
			return Mth.clamp(overlayPosX, 0.0F, 1.0F);
		}

		@Override
		public float getOverlayPosY() {
			return Mth.clamp(overlayPosY, 0.0F, 1.0F);
		}

		@Override
		public float getOverlayScale() {
			return overlayScale;
		}

		@Override
		public float getAnchorX() {
			return Mth.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		@Override
		public float getAnchorY() {
			return Mth.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		@Override
		public void setFlipMainHand(boolean overlaySquare) {
			flipMainHand = overlaySquare;
		}

		@Override
		public boolean getFlipMainHand() {
			return flipMainHand;
		}

		@Override
		public float tryFlip(float f) {
			if (Minecraft.getInstance().options.mainHand == HumanoidArm.LEFT)
				f = 1 - f;
			return f;
		}

		@Override
		public void setSquare(boolean overlaySquare) {
			this.overlaySquare = overlaySquare;
		}

		@Override
		public boolean getSquare() {
			return overlaySquare;
		}

		@Override
		public float getAutoScaleThreshold() {
			return autoScaleThreshold;
		}

		@Override
		public float getAlpha() {
			return alpha;
		}

		@Override
		public Theme getTheme() {
			return themes.getOrDefault(activeTheme, Theme.DARK);
		}

		@Override
		public Collection<Theme> getThemes() {
			return themes.values();
		}

		@Override
		public void setAlpha(float alpha) {
			this.alpha = Mth.clamp(alpha, 0, 1);
		}

		@Override
		public void applyTheme(ResourceLocation id) {
			activeTheme = themes.containsKey(id) ? id : activeTheme;
			OverlayRenderer.updateTheme();
		}

		@Override
		public void setIconMode(IconMode iconMode) {
			this.iconMode = iconMode;
		}

		@Override
		public IconMode getIconMode() {
			return iconMode;
		}

		@Override
		public boolean shouldShowIcon() {
			return iconMode != IconMode.HIDE;
		}

	}

	public static class ConfigFormatting implements IConfigFormatting {
		private String modName = "§9§o%s";
		private String titleName = "%s";
		private String registryName = "§7[%s]";

		@Override
		public void setModName(String modName) {
			this.modName = modName;
		}

		@Override
		public void setTitleName(String titleName) {
			this.titleName = titleName;
		}

		@Override
		public void setRegistryName(String registryName) {
			this.registryName = registryName;
		}

		@Override
		public String getModName() {
			return modName;
		}

		@Override
		public String getTitleName() {
			return titleName;
		}

		@Override
		public String getRegistryName() {
			return registryName;
		}

		@Override
		public Component title(Object title) {
			if (title instanceof Component) {
				title = ((Component) title).getString();
			}
			return new TextComponent(String.format(titleName, title)).withStyle($ -> $.withColor(OverlayRenderer.stressedTextColorRaw));
		}
	}

}
