package snownee.jade.impl.config;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.HumanoidArm;
import snownee.jade.Jade;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeCodecs;
import snownee.jade.util.ModIdentification;

/**
 * Get this instance from {@link IWailaConfig#get()}
 */
public class WailaConfig implements IWailaConfig {
	public static final Codec<WailaConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			ConfigGeneral.CODEC.fieldOf("general")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(ConfigGeneral.CODEC))
					.forGetter(WailaConfig::getGeneral),
			ConfigOverlay.CODEC.fieldOf("overlay")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(ConfigOverlay.CODEC))
					.forGetter(WailaConfig::getOverlay),
			ConfigFormatting.CODEC.fieldOf("formatting")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(ConfigFormatting.CODEC))
					.forGetter(WailaConfig::getFormatting),
			ConfigHistory.CODEC.fieldOf("history")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(ConfigHistory.CODEC))
					.forGetter(WailaConfig::getHistory)
	).apply(i, WailaConfig::new));

	private final ConfigGeneral general;
	private final ConfigOverlay overlay;
	private final ConfigFormatting formatting;
	private final ConfigHistory history;

	public WailaConfig(ConfigGeneral general, ConfigOverlay overlay, ConfigFormatting formatting, ConfigHistory history) {
		this.general = general;
		this.overlay = overlay;
		this.formatting = formatting;
		this.history = history;
	}

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

	public ConfigHistory getHistory() {
		return history;
	}

	@Override
	public IPluginConfig getPlugin() {
		return PluginConfig.INSTANCE;
	}

	public static class ConfigHistory {

		public static final Codec<ConfigHistory> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.optionalFieldOf("hintOverlayToggle", true).forGetter($ -> $.hintOverlayToggle),
				Codec.BOOL.optionalFieldOf("hintNarratorToggle", true).forGetter($ -> $.hintNarratorToggle),
				Codec.INT.optionalFieldOf("themesHash", 0).forGetter($ -> $.themesHash)
		).apply(i, ConfigHistory::new));

		public boolean hintOverlayToggle;
		public boolean hintNarratorToggle;
		public int themesHash;

		public ConfigHistory(boolean hintOverlayToggle, boolean hintNarratorToggle, int themesHash) {
			this.hintOverlayToggle = hintOverlayToggle;
			this.hintNarratorToggle = hintNarratorToggle;
			this.themesHash = themesHash;
		}
	}

	public static class ConfigGeneral implements IConfigGeneral {

		public static final Codec<ConfigGeneral> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.fieldOf("previewOverlay").orElse(true).forGetter($ -> $.previewOverlay),
				Codec.BOOL.fieldOf("displayTooltip").orElse(true).forGetter(ConfigGeneral::shouldDisplayTooltip),
				Codec.BOOL.fieldOf("displayBlocks").orElse(true).forGetter(ConfigGeneral::getDisplayBlocks),
				Codec.BOOL.fieldOf("displayEntities").orElse(true).forGetter(ConfigGeneral::getDisplayEntities),
				Codec.BOOL.fieldOf("displayBosses").orElse(true).forGetter(ConfigGeneral::getDisplayBosses),
				StringRepresentable.fromEnum(DisplayMode::values)
						.fieldOf("displayMode").orElse(DisplayMode.TOGGLE)
						.forGetter(ConfigGeneral::getDisplayMode),
				Codec.BOOL.fieldOf("enableTextToSpeech").orElse(false).forGetter(ConfigGeneral::shouldEnableTextToSpeech),
				StringRepresentable.fromEnum(TTSMode::values)
						.fieldOf("ttsMode").orElse(TTSMode.PRESS)
						.forGetter(ConfigGeneral::getTTSMode),
				StringRepresentable.fromEnum(FluidMode::values)
						.fieldOf("fluidMode").orElse(FluidMode.ANY)
						.forGetter(ConfigGeneral::getDisplayFluids),
				StringRepresentable.fromEnum(PerspectiveMode::values)
						.fieldOf("perspectiveMode").orElse(PerspectiveMode.CAMERA)
						.forGetter(ConfigGeneral::getPerspectiveMode),
				Codec.floatRange(0, 20).fieldOf("extendedReach").orElse(0F).forGetter(ConfigGeneral::getExtendedReach),
				Codec.BOOL.fieldOf("debug").orElse(false).forGetter(ConfigGeneral::isDebug),
				Codec.BOOL.fieldOf("itemModNameTooltip").orElse(true).forGetter(ConfigGeneral::showItemModNameTooltip),
				StringRepresentable.fromEnum(BossBarOverlapMode::values)
						.fieldOf("bossBarOverlapMode").orElse(BossBarOverlapMode.PUSH_DOWN)
						.forGetter(ConfigGeneral::getBossBarOverlapMode),
				Codec.BOOL.fieldOf("builtinCamouflage").orElse(true).forGetter(ConfigGeneral::getBuiltinCamouflage),
				ExtraOptions.CODEC.orElseGet(() -> JadeCodecs.createFromEmptyMap(ExtraOptions.CODEC.codec())).forGetter($ -> $.extraOptions)
		).apply(i, ConfigGeneral::new));

		public static final List<String> itemModNameTooltipDisabledByMods = Lists.newArrayList("emi");
		public boolean previewOverlay;
		private boolean displayTooltip;
		private boolean displayBlocks;
		private boolean displayEntities;
		private boolean displayBosses;
		private DisplayMode displayMode;
		private boolean enableTextToSpeech;
		private TTSMode ttsMode;
		private FluidMode fluidMode;
		private float extendedReach;
		private boolean debug;
		private boolean itemModNameTooltip;
		private BossBarOverlapMode bossBarOverlapMode;
		private boolean builtinCamouflage;
		private PerspectiveMode perspectiveMode;
		private final ExtraOptions extraOptions;

		public static final class ExtraOptions {
			public static final MapCodec<ExtraOptions> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
					Codec.BOOL.fieldOf("hideFromTabList").orElse(true).forGetter(ExtraOptions::hideFromTabList),
					Codec.BOOL.fieldOf("hideFromGUIs").orElse(true).forGetter(ExtraOptions::hideFromGUIs),
					Codec.BOOL.fieldOf("accessibilityModMemory").orElse(false).forGetter(ExtraOptions::accessibilityModMemory),
					Codec.BOOL.fieldOf("enableAccessibilityPlugin").orElse(false).forGetter(ExtraOptions::enableAccessibilityPlugin)
			).apply(i, ExtraOptions::new));

			private boolean hideFromTabList;
			private boolean hideFromGUIs;
			public boolean accessibilityModMemory;
			public boolean enableAccessibilityPlugin;

			public ExtraOptions(
					boolean hideFromTabList,
					boolean hideFromGUIs,
					boolean accessibilityModMemory,
					boolean enableAccessibilityPlugin) {
				this.hideFromTabList = hideFromTabList;
				this.hideFromGUIs = hideFromGUIs;
				this.accessibilityModMemory = accessibilityModMemory;
				this.enableAccessibilityPlugin = enableAccessibilityPlugin;
			}

			public boolean hideFromTabList() {
				return hideFromTabList;
			}

			public boolean hideFromGUIs() {
				return hideFromGUIs;
			}

			public boolean accessibilityModMemory() {
				return accessibilityModMemory;
			}

			public boolean enableAccessibilityPlugin() {
				return enableAccessibilityPlugin;
			}

			public void setHideFromTabList(boolean hideFromTabList) {
				this.hideFromTabList = hideFromTabList;
			}

			public void setHideFromGUIs(boolean hideFromGUIs) {
				this.hideFromGUIs = hideFromGUIs;
			}

			public void setAccessibilityModMemory(boolean accessibilityModMemory) {
				this.accessibilityModMemory = accessibilityModMemory;
			}

			public void setEnableAccessibilityPlugin(boolean enableAccessibilityPlugin) {
				this.enableAccessibilityPlugin = enableAccessibilityPlugin;
			}
		}

		public ConfigGeneral(
				boolean previewOverlay,
				boolean displayTooltip,
				boolean displayBlocks,
				boolean displayEntities,
				boolean displayBosses,
				DisplayMode displayMode,
				boolean enableTextToSpeech,
				TTSMode ttsMode,
				FluidMode fluidMode,
				PerspectiveMode perspectiveMode,
				float extendedReach,
				boolean debug,
				boolean itemModNameTooltip,
				BossBarOverlapMode bossBarOverlapMode,
				boolean builtinCamouflage,
				ExtraOptions extraOptions) {
			this.previewOverlay = previewOverlay;
			this.displayTooltip = displayTooltip;
			this.displayBlocks = displayBlocks;
			this.displayEntities = displayEntities;
			this.displayBosses = displayBosses;
			this.displayMode = displayMode;
			this.enableTextToSpeech = enableTextToSpeech;
			this.ttsMode = ttsMode;
			this.fluidMode = fluidMode;
			this.perspectiveMode = perspectiveMode;
			this.extendedReach = extendedReach;
			this.debug = debug;
			this.itemModNameTooltip = itemModNameTooltip;
			this.bossBarOverlapMode = bossBarOverlapMode;
			this.builtinCamouflage = builtinCamouflage;
			this.extraOptions = extraOptions;
		}

		public static void init() {
			/* off */
			List<String> names = itemModNameTooltipDisabledByMods.stream()
					.filter(CommonProxy::isModLoaded)
					.map($ -> ModIdentification.getModName($).orElse($))
					.toList();
			/* on */
			itemModNameTooltipDisabledByMods.clear();
			itemModNameTooltipDisabledByMods.addAll(names);

			IWailaConfig.IConfigGeneral config = IWailaConfig.get().getGeneral();
			boolean hasAccessibilityMod = ClientProxy.hasAccessibilityMod();
			if (config.getAccessibilityModMemory() != hasAccessibilityMod) {
				config.setAccessibilityModMemory(hasAccessibilityMod);
				config.setEnableAccessibilityPlugin(hasAccessibilityMod);
				Jade.CONFIG.save();
			}
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
			this.extraOptions.setHideFromTabList(hideFromTabList);
		}

		@Override
		public void setHideFromGUIs(boolean hideFromGUIs) {
			this.extraOptions.setHideFromGUIs(hideFromGUIs);
		}

		@Override
		public boolean shouldHideFromTabList() {
			return extraOptions.hideFromTabList();
		}

		@Override
		public boolean shouldHideFromGUIs() {
			return extraOptions.hideFromGUIs();
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

		@Override
		public boolean getAccessibilityModMemory() {
			return extraOptions.accessibilityModMemory();
		}

		@Override
		public void setAccessibilityModMemory(boolean accessibilityModMemory) {
			extraOptions.setAccessibilityModMemory(accessibilityModMemory);
		}

		@Override
		public boolean getEnableAccessibilityPlugin() {
			return extraOptions.enableAccessibilityPlugin();
		}

		@Override
		public void setEnableAccessibilityPlugin(boolean enableAccessibilityPlugin) {
			extraOptions.setEnableAccessibilityPlugin(enableAccessibilityPlugin);
		}

		@Override
		public PerspectiveMode getPerspectiveMode() {
			return perspectiveMode;
		}

		@Override
		public void setPerspectiveMode(PerspectiveMode perspectiveMode) {
			this.perspectiveMode = perspectiveMode;
		}
	}

	public static class ConfigOverlay implements IConfigOverlay {

		public static final Codec<ConfigOverlay> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.fieldOf("activeTheme").orElse(Theme.DEFAULT_THEME_ID).forGetter($ -> $.activeTheme),
				Codec.FLOAT.fieldOf("overlayPosX").orElse(0.5F).forGetter(ConfigOverlay::getOverlayPosX),
				Codec.FLOAT.fieldOf("overlayPosY").orElse(1.0F).forGetter(ConfigOverlay::getOverlayPosY),
				Codec.floatRange(0.2F, 2F).fieldOf("overlayScale").orElse(1.0F).forGetter(ConfigOverlay::getOverlayScale),
				Codec.FLOAT.fieldOf("overlayAnchorX").orElse(0.5F).forGetter(ConfigOverlay::getAnchorX),
				Codec.FLOAT.fieldOf("overlayAnchorY").orElse(0.0F).forGetter(ConfigOverlay::getAnchorY),
				Codec.BOOL.fieldOf("overlaySquare").orElse(false).forGetter(ConfigOverlay::getSquare),
				Codec.BOOL.fieldOf("flipMainHand").orElse(false).forGetter(ConfigOverlay::getFlipMainHand),
				Codec.floatRange(0, 1).fieldOf("autoScaleThreshold").orElse(0.4f).forGetter(ConfigOverlay::getAutoScaleThreshold),
				Codec.floatRange(0, 1).fieldOf("alpha").orElse(0.7f).forGetter(ConfigOverlay::getAlpha),
				StringRepresentable.fromEnum(IconMode::values)
						.fieldOf("iconMode").orElse(IconMode.TOP)
						.forGetter(ConfigOverlay::getIconMode),
				Codec.BOOL.fieldOf("animation").orElse(true).forGetter(ConfigOverlay::getAnimation),
				Codec.floatRange(0, Float.MAX_VALUE).fieldOf("disappearingDelay").orElse(0F).forGetter(ConfigOverlay::getDisappearingDelay)
		).apply(i, ConfigOverlay::new));

		public ResourceLocation activeTheme;
		private float overlayPosX;
		private float overlayPosY;
		private float overlayScale;
		private float overlayAnchorX;
		private float overlayAnchorY;
		private boolean overlaySquare;
		private boolean flipMainHand;
		private float autoScaleThreshold;
		private float alpha;
		private transient Theme activeThemeInstance;
		private IconMode iconMode;
		private boolean animation;
		private float disappearingDelay;

		public ConfigOverlay(
				ResourceLocation activeTheme,
				float overlayPosX,
				float overlayPosY,
				float overlayScale,
				float overlayAnchorX,
				float overlayAnchorY,
				boolean overlaySquare,
				boolean flipMainHand,
				float autoScaleThreshold,
				float alpha,
				IconMode iconMode,
				boolean animation,
				float disappearingDelay) {
			this.activeTheme = activeTheme;
			this.overlayPosX = overlayPosX;
			this.overlayPosY = overlayPosY;
			this.overlayScale = overlayScale;
			this.overlayAnchorX = overlayAnchorX;
			this.overlayAnchorY = overlayAnchorY;
			this.overlaySquare = overlaySquare;
			this.flipMainHand = flipMainHand;
			this.autoScaleThreshold = autoScaleThreshold;
			this.alpha = alpha;
			this.iconMode = iconMode;
			this.animation = animation;
			this.disappearingDelay = disappearingDelay;
		}

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
			if (flipMainHand && Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT) {
				f = 1 - f;
			}
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
			if (activeThemeInstance == null) {
				applyTheme(activeTheme);
			}
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

		public static final Codec<ConfigFormatting> CODEC = RecordCodecBuilder.create(i -> i.group(
				Style.Serializer.CODEC.fieldOf("itemModNameStyle")
						.orElseGet(() -> Style.EMPTY.applyFormats(ChatFormatting.BLUE, ChatFormatting.ITALIC))
						.forGetter(ConfigFormatting::getItemModNameStyle)
		).apply(i, ConfigFormatting::new));

		private Style itemModNameStyle;

		public ConfigFormatting(Style itemModNameStyle) {
			this.itemModNameStyle = itemModNameStyle;
		}

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
			return Component.literal(name).withStyle(IThemeHelper.get().isLightColorScheme() ?
					ChatFormatting.DARK_GRAY :
					ChatFormatting.GRAY);
		}
	}

}
