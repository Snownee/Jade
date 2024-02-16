package snownee.jade.impl.config;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

/**
 * Get this instance from {@link IWailaConfig#get()}
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

	@Override
	public IPluginConfig getPlugin() {
		return PluginConfig.INSTANCE;
	}

	public static class ConfigGeneral implements IConfigGeneral {
		public static final List<String> itemModNameTooltipDisabledByMods = Lists.newArrayList("emi");
		public boolean hintOverlayToggle = true;
		public boolean hintNarratorToggle = true;
		public boolean previewOverlay = true;
		private boolean displayTooltip = true;
		private boolean displayBlocks = true;
		private boolean displayEntities = true;
		private boolean displayBosses = true;
		private DisplayMode displayMode = DisplayMode.TOGGLE;
		private boolean hideFromDebug = true;
		private boolean hideFromTabList = true;
		private boolean enableTextToSpeech = false;
		private TTSMode ttsMode = TTSMode.PRESS;
		private FluidMode fluidMode = FluidMode.ANY;
		private float extendedReach = 0;
		@Expose
		private boolean debug = false;
		private boolean itemModNameTooltip = true;
		private BossBarOverlapMode bossBarOverlapMode = BossBarOverlapMode.PUSH_DOWN;
		private boolean builtinCamouflage = true;

		public static void init() {
			/* off */
			List<String> names = itemModNameTooltipDisabledByMods.stream()
					.filter(CommonProxy::isModLoaded)
					.map(ModIdentification::getModName)
					.map(Optional::orElseThrow)
					.toList();
			/* on */
			itemModNameTooltipDisabledByMods.clear();
			itemModNameTooltipDisabledByMods.addAll(names);
		}

		@Override
		public void setDisplayTooltip(boolean displayTooltip) {
			this.displayTooltip = displayTooltip;
		}

		@Override
		public boolean getDisplayEntities() {
			return displayEntities;
		}

		@Override
		public void setDisplayEntities(boolean displayEntities) {
			this.displayEntities = displayEntities;
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
		public void setHideFromDebug(boolean hideFromDebug) {
			this.hideFromDebug = hideFromDebug;
		}

		@Override
		public void toggleTTS() {
			enableTextToSpeech = !enableTextToSpeech;
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
		public void setDisplayMode(DisplayMode displayMode) {
			this.displayMode = displayMode;
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
		public void setTTSMode(TTSMode ttsMode) {
			this.ttsMode = ttsMode;
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
		public void setDisplayFluids(boolean displayFluids) {
			fluidMode = displayFluids ? FluidMode.ANY : FluidMode.NONE;
		}

		@Override
		public void setDisplayFluids(FluidMode displayFluids) {
			fluidMode = displayFluids;
		}

		@Override
		public float getExtendedReach() {
			return extendedReach;
		}

		@Override
		public void setExtendedReach(float extendedReach) {
			this.extendedReach = Mth.clamp(extendedReach, 0, 20);
		}

		@Override
		public boolean isDebug() {
			return debug;
		}

		@Override
		public void setDebug(boolean debug) {
			this.debug = debug;
		}

		@Override
		public void setItemModNameTooltip(boolean itemModNameTooltip) {
			this.itemModNameTooltip = itemModNameTooltip;
		}

		@Override
		public boolean showItemModNameTooltip() {
			return itemModNameTooltip && itemModNameTooltipDisabledByMods.isEmpty();
		}

		@Override
		public BossBarOverlapMode getBossBarOverlapMode() {
			return bossBarOverlapMode;
		}

		@Override
		public void setBossBarOverlapMode(BossBarOverlapMode mode) {
			bossBarOverlapMode = mode;
		}

		@Override
		public void setHideFromTabList(boolean hideFromTabList) {
			this.hideFromTabList = hideFromTabList;
		}

		@Override
		public boolean shouldHideFromTabList() {
			return hideFromTabList;
		}

		@Override
		public boolean getDisplayBosses() {
			return displayBosses;
		}

		@Override
		public void setDisplayBosses(boolean displayBosses) {
			this.displayBosses = displayBosses;
		}

		@Override
		public boolean getBuiltinCamouflage() {
			return builtinCamouflage;
		}

		@Override
		public void setBuiltinCamouflage(boolean builtinCamouflage) {
			this.builtinCamouflage = builtinCamouflage;
		}

	}

	public static class ConfigOverlay implements IConfigOverlay {
		public ResourceLocation activeTheme = Theme.DEFAULT_THEME_ID;
		public int themesHash;
		private float overlayPosX = 0.5F;
		private float overlayPosY = 1.0F;
		private float overlayScale = 1.0F;
		private float overlayAnchorX = 0.5F;
		private float overlayAnchorY = 0F;
		private boolean overlaySquare = false;
		private boolean flipMainHand = false;
		@Expose
		private float autoScaleThreshold = 0.4f;
		private float alpha = 0.7f;
		private transient Theme activeThemeInstance;
		private IconMode iconMode = IconMode.TOP;
		private boolean animation = true;
		private float disappearingDelay;

		@Override
		public float getOverlayPosX() {
			return Mth.clamp(overlayPosX, 0.0F, 1.0F);
		}

		@Override
		public void setOverlayPosX(float overlayPosX) {
			this.overlayPosX = Mth.clamp(overlayPosX, 0.0F, 1.0F);
		}

		@Override
		public float getOverlayPosY() {
			return Mth.clamp(overlayPosY, 0.0F, 1.0F);
		}

		@Override
		public void setOverlayPosY(float overlayPosY) {
			this.overlayPosY = Mth.clamp(overlayPosY, 0.0F, 1.0F);
		}

		@Override
		public float getOverlayScale() {
			return overlayScale;
		}

		@Override
		public void setOverlayScale(float overlayScale) {
			this.overlayScale = Mth.clamp(overlayScale, 0.2F, 2.0F);
		}

		@Override
		public float getAnchorX() {
			return Mth.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		@Override
		public void setAnchorX(float overlayAnchorX) {
			this.overlayAnchorX = Mth.clamp(overlayAnchorX, 0.0F, 1.0F);
		}

		@Override
		public float getAnchorY() {
			return Mth.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		@Override
		public void setAnchorY(float overlayAnchorY) {
			this.overlayAnchorY = Mth.clamp(overlayAnchorY, 0.0F, 1.0F);
		}

		@Override
		public boolean getFlipMainHand() {
			return flipMainHand;
		}

		@Override
		public void setFlipMainHand(boolean overlaySquare) {
			flipMainHand = overlaySquare;
		}

		@Override
		public float tryFlip(float f) {
			if (flipMainHand && Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT)
				f = 1 - f;
			return f;
		}

		@Override
		public boolean getSquare() {
			return overlaySquare;
		}

		@Override
		public void setSquare(boolean overlaySquare) {
			this.overlaySquare = overlaySquare;
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
		public void setAlpha(float alpha) {
			this.alpha = Mth.clamp(alpha, 0, 1);
		}

		@Override
		public Theme getTheme() {
			if (activeThemeInstance == null)
				applyTheme(activeTheme);
			return activeThemeInstance;
		}

		@Override
		public void applyTheme(ResourceLocation id) {
			activeThemeInstance = IThemeHelper.get().getTheme(id);
			activeTheme = activeThemeInstance.id;
		}

		@Override
		public IconMode getIconMode() {
			return iconMode;
		}

		@Override
		public void setIconMode(IconMode iconMode) {
			this.iconMode = iconMode;
		}

		@Override
		public boolean shouldShowIcon() {
			return iconMode != IconMode.HIDE;
		}

		@Override
		public boolean getAnimation() {
			return animation;
		}

		@Override
		public void setAnimation(boolean animation) {
			this.animation = animation;
		}

		@Override
		public float getDisappearingDelay() {
			return disappearingDelay;
		}

		@Override
		public void setDisappearingDelay(float delay) {
			disappearingDelay = delay;
		}

	}

	public static class ConfigFormatting implements IConfigFormatting {
		private Style itemModNameStyle = Style.EMPTY.applyFormats(ChatFormatting.BLUE, ChatFormatting.ITALIC);

		@Override
		public Style getItemModNameStyle() {
			return itemModNameStyle;
		}

		@Override
		public void setItemModNameStyle(Style itemModNameStyle) {
			this.itemModNameStyle = itemModNameStyle;
		}

		@Override
		public Component registryName(String name) {
			return Component.literal(name).withStyle(IThemeHelper.get().isLightColorScheme() ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY);
		}
	}

}
