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
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeCodecs;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.ModIdentification;

/**
 * Get this instance from {@link IWailaConfig#get()}
 */
public class WailaConfig implements IWailaConfig {
	public static final MapCodec<WailaConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.string(0, 32).optionalFieldOf("name", "").forGetter(IWailaConfig::getName),
			General.CODEC.fieldOf("general")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(General.CODEC))
					.forGetter(WailaConfig::general),
			Overlay.CODEC.fieldOf("overlay")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(Overlay.CODEC))
					.forGetter(WailaConfig::overlay),
			Formatting.CODEC.fieldOf("formatting")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(Formatting.CODEC))
					.forGetter(WailaConfig::formatting),
			Accessibility.CODEC.fieldOf("accessibility")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(Accessibility.CODEC))
					.forGetter(WailaConfig::accessibility),
			PluginConfig.CODEC.fieldOf("plugin")
					.orElseGet(() -> JadeCodecs.createFromEmptyMap(PluginConfig.CODEC))
					.forGetter(WailaConfig::plugin)
	).apply(i, WailaConfig::new));

	private String name;
	private final General general;
	private final Overlay overlay;
	private final Formatting formatting;
	private final Accessibility accessibility;
	private final PluginConfig plugin;

	public WailaConfig(
			String name,
			General general,
			Overlay overlay,
			Formatting formatting,
			Accessibility accessibility,
			PluginConfig plugin) {
		this.name = name;
		this.general = general;
		this.overlay = overlay;
		this.formatting = formatting;
		this.accessibility = accessibility;
		this.plugin = plugin;
	}

	@Override
	public General general() {
		return general;
	}

	@Override
	public Overlay overlay() {
		return overlay;
	}

	@Override
	public Formatting formatting() {
		return formatting;
	}

	@Override
	public Accessibility accessibility() {
		return accessibility;
	}

	@Override
	public PluginConfig plugin() {
		return plugin;
	}

	@Override
	public void save() {
		Jade.saveConfig();
	}

	@Override
	public void invalidate() {
		Jade.invalidateConfig();
	}

	public void fixData() {
		for (ConfigEntry<?> entry : WailaClientRegistration.instance().configEntries.values()) {
			plugin.ensureEntry(entry);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public static void init() {
		/* off */
		List<String> names = General.itemModNameTooltipDisabledByMods.stream()
				.filter(CommonProxy::isModLoaded)
				.map($ -> ModIdentification.getModName($).orElse($))
				.toList();
		/* on */
		General.itemModNameTooltipDisabledByMods.clear();
		General.itemModNameTooltipDisabledByMods.addAll(names);

		boolean hasAccessibilityMod = ClientProxy.hasAccessibilityMod();
		if (Jade.history().accessibilityModMemory != hasAccessibilityMod) {
			Jade.history().accessibilityModMemory = hasAccessibilityMod;
			for (JsonConfig<? extends WailaConfig> config : Jade.configs()) {
				config.get().accessibility().setEnableAccessibilityPlugin(hasAccessibilityMod);
				config.save();
			}
		}
	}

	public static class History {

		public static final Codec<History> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.fieldOf("previewOverlay").orElse(true).forGetter($ -> $.previewOverlay),
				Codec.BOOL.fieldOf("hintOverlayToggle").orElse(true).forGetter($ -> $.hintOverlayToggle),
				Codec.BOOL.fieldOf("hintNarratorToggle").orElse(true).forGetter($ -> $.hintNarratorToggle),
				Codec.BOOL.fieldOf("accessibilityModMemory").orElse(false).forGetter($ -> $.accessibilityModMemory),
				Codec.INT.fieldOf("themesHash").orElse(0).forGetter($ -> $.themesHash)
		).apply(i, History::new));

		public boolean previewOverlay;
		public boolean hintOverlayToggle;
		public boolean hintNarratorToggle;
		public boolean accessibilityModMemory;
		public int themesHash;

		public History(
				boolean previewOverlay,
				boolean hintOverlayToggle,
				boolean hintNarratorToggle,
				boolean accessibilityModMemory,
				int themesHash) {
			this.previewOverlay = previewOverlay;
			this.hintOverlayToggle = hintOverlayToggle;
			this.hintNarratorToggle = hintNarratorToggle;
			this.accessibilityModMemory = accessibilityModMemory;
			this.themesHash = themesHash;
		}
	}

	public static class General implements IWailaConfig.General {

		public static final Codec<General> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.fieldOf("displayTooltip").orElse(true).forGetter(General::shouldDisplayTooltip),
				Codec.BOOL.fieldOf("displayBlocks").orElse(true).forGetter(General::getDisplayBlocks),
				Codec.BOOL.fieldOf("displayEntities").orElse(true).forGetter(General::getDisplayEntities),
				Codec.BOOL.fieldOf("displayBosses").orElse(true).forGetter(General::getDisplayBosses),
				StringRepresentable.fromEnum(DisplayMode::values)
						.fieldOf("displayMode").orElse(DisplayMode.TOGGLE)
						.forGetter(General::getDisplayMode),
				StringRepresentable.fromEnum(FluidMode::values)
						.fieldOf("fluidMode").orElse(FluidMode.ANY)
						.forGetter(General::getDisplayFluids),
				Codec.floatRange(0, 20).fieldOf("extendedReach").orElse(0F).forGetter(General::getExtendedReach),
				Codec.BOOL.fieldOf("debug").orElse(false).forGetter(General::isDebug),
				Codec.BOOL.fieldOf("itemModNameTooltip").orElse(true).forGetter(General::showItemModNameTooltip),
				StringRepresentable.fromEnum(BossBarOverlapMode::values)
						.fieldOf("bossBarOverlapMode").orElse(BossBarOverlapMode.PUSH_DOWN)
						.forGetter(General::getBossBarOverlapMode),
				Codec.BOOL.fieldOf("builtinCamouflage").orElse(true).forGetter(General::getBuiltinCamouflage),
				ExtraOptions.CODEC.orElseGet(() -> JadeCodecs.createFromEmptyMap(ExtraOptions.CODEC.codec())).forGetter($ -> $.extraOptions)
		).apply(i, General::new));

		public static final List<String> itemModNameTooltipDisabledByMods = Lists.newArrayList("emi");
		private boolean displayTooltip;
		private boolean displayBlocks;
		private boolean displayEntities;
		private boolean displayBosses;
		private DisplayMode displayMode;
		private FluidMode fluidMode;
		private float extendedReach;
		private boolean debug;
		private boolean itemModNameTooltip;
		private BossBarOverlapMode bossBarOverlapMode;
		private boolean builtinCamouflage;
		private final ExtraOptions extraOptions;

		public static final class ExtraOptions {
			public static final MapCodec<ExtraOptions> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
					Codec.BOOL.fieldOf("hideFromDebug").orElse(true).forGetter(ExtraOptions::hideFromDebug),
					Codec.BOOL.fieldOf("hideFromTabList").orElse(true).forGetter(ExtraOptions::hideFromTabList),
					Codec.BOOL.fieldOf("hideFromGUIs").orElse(true).forGetter(ExtraOptions::hideFromGUIs)
			).apply(i, ExtraOptions::new));

			private boolean hideFromDebug;
			private boolean hideFromTabList;
			private boolean hideFromGUIs;

			public ExtraOptions(
					boolean hideFromDebug,
					boolean hideFromTabList,
					boolean hideFromGUIs) {
				this.hideFromDebug = hideFromDebug;
				this.hideFromTabList = hideFromTabList;
				this.hideFromGUIs = hideFromGUIs;
			}

			public boolean hideFromDebug() {
				return hideFromDebug;
			}

			public boolean hideFromTabList() {
				return hideFromTabList;
			}

			public boolean hideFromGUIs() {
				return hideFromGUIs;
			}

			public void setHideFromDebug(boolean hideFromDebug) {
				this.hideFromDebug = hideFromDebug;
			}

			public void setHideFromTabList(boolean hideFromTabList) {
				this.hideFromTabList = hideFromTabList;
			}

			public void setHideFromGUIs(boolean hideFromGUIs) {
				this.hideFromGUIs = hideFromGUIs;
			}
		}

		public General(
				boolean displayTooltip,
				boolean displayBlocks,
				boolean displayEntities,
				boolean displayBosses,
				DisplayMode displayMode,
				FluidMode fluidMode,
				float extendedReach,
				boolean debug,
				boolean itemModNameTooltip,
				BossBarOverlapMode bossBarOverlapMode,
				boolean builtinCamouflage,
				ExtraOptions extraOptions) {
			this.displayTooltip = displayTooltip;
			this.displayBlocks = displayBlocks;
			this.displayEntities = displayEntities;
			this.displayBosses = displayBosses;
			this.displayMode = displayMode;
			this.fluidMode = fluidMode;
			this.extendedReach = extendedReach;
			this.debug = debug;
			this.itemModNameTooltip = itemModNameTooltip;
			this.bossBarOverlapMode = bossBarOverlapMode;
			this.builtinCamouflage = builtinCamouflage;
			this.extraOptions = extraOptions;
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
			this.extraOptions.setHideFromDebug(hideFromDebug);
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
			return extraOptions.hideFromDebug();
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
	}

	public static class Overlay implements IWailaConfig.Overlay {

		public static final Codec<Overlay> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.fieldOf("activeTheme").orElse(Theme.DEFAULT_THEME_ID).forGetter($ -> $.activeTheme),
				Codec.FLOAT.fieldOf("overlayPosX").orElse(0.5F).forGetter(Overlay::getOverlayPosX),
				Codec.FLOAT.fieldOf("overlayPosY").orElse(1.0F).forGetter(Overlay::getOverlayPosY),
				Codec.floatRange(0.2F, 2F).fieldOf("overlayScale").orElse(1.0F).forGetter(Overlay::getOverlayScale),
				Codec.FLOAT.fieldOf("overlayAnchorX").orElse(0.5F).forGetter(Overlay::getAnchorX),
				Codec.FLOAT.fieldOf("overlayAnchorY").orElse(0.0F).forGetter(Overlay::getAnchorY),
				Codec.BOOL.fieldOf("overlaySquare").orElse(false).forGetter(Overlay::getSquare),
				Codec.floatRange(0, 1).fieldOf("autoScaleThreshold").orElse(0.4f).forGetter(Overlay::getAutoScaleThreshold),
				Codec.floatRange(0, 1).fieldOf("alpha").orElse(0.7f).forGetter(Overlay::getAlpha),
				StringRepresentable.fromEnum(IconMode::values)
						.fieldOf("iconMode").orElse(IconMode.TOP)
						.forGetter(Overlay::getIconMode),
				Codec.BOOL.fieldOf("animation").orElse(true).forGetter(Overlay::getAnimation),
				Codec.floatRange(0, Float.MAX_VALUE).fieldOf("disappearingDelay").orElse(0F).forGetter(Overlay::getDisappearingDelay)
		).apply(i, Overlay::new));

		public ResourceLocation activeTheme;
		private float overlayPosX;
		private float overlayPosY;
		private float overlayScale;
		private float overlayAnchorX;
		private float overlayAnchorY;
		private boolean overlaySquare;
		private float autoScaleThreshold;
		private float alpha;
		private transient Theme activeThemeInstance;
		private IconMode iconMode;
		private boolean animation;
		private float disappearingDelay;

		public Overlay(
				ResourceLocation activeTheme,
				float overlayPosX,
				float overlayPosY,
				float overlayScale,
				float overlayAnchorX,
				float overlayAnchorY,
				boolean overlaySquare,
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

	public static class Formatting implements IWailaConfig.Formatting {

		public static final Codec<Formatting> CODEC = RecordCodecBuilder.create(i -> i.group(
				Style.Serializer.CODEC.fieldOf("itemModNameStyle")
						.orElseGet(() -> Style.EMPTY.applyFormats(ChatFormatting.BLUE, ChatFormatting.ITALIC))
						.forGetter(Formatting::getItemModNameStyle)
		).apply(i, Formatting::new));

		private Style itemModNameStyle;

		public Formatting(Style itemModNameStyle) {
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

	public static class Accessibility implements IWailaConfig.Accessibility {

		public static final Codec<Accessibility> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.fieldOf("enableTextToSpeech").orElse(false).forGetter(Accessibility::shouldEnableTextToSpeech),
				StringRepresentable.fromEnum(TTSMode::values)
						.fieldOf("ttsMode").orElse(TTSMode.TOGGLE)
						.forGetter(Accessibility::getTTSMode),
				Codec.BOOL.fieldOf("enableAccessibilityPlugin").orElse(false).forGetter(Accessibility::getEnableAccessibilityPlugin),
				Codec.BOOL.fieldOf("flipMainHand").orElse(false).forGetter(Accessibility::getFlipMainHand)
		).apply(i, Accessibility::new));

		private boolean enableTextToSpeech;
		private TTSMode ttsMode;
		private boolean enableAccessibilityPlugin;
		private boolean flipMainHand;

		public Accessibility(boolean enableTextToSpeech, TTSMode ttsMode, boolean enableAccessibilityPlugin, boolean flipMainHand) {
			this.enableTextToSpeech = enableTextToSpeech;
			this.ttsMode = ttsMode;
			this.enableAccessibilityPlugin = enableAccessibilityPlugin;
			this.flipMainHand = flipMainHand;
		}

		@Override
		public boolean shouldEnableTextToSpeech() {
			return ttsMode == TTSMode.TOGGLE && enableTextToSpeech;
		}

		@Override
		public void toggleTTS() {
			enableTextToSpeech = !enableTextToSpeech;
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
		public boolean getEnableAccessibilityPlugin() {
			return enableAccessibilityPlugin;
		}

		@Override
		public void setEnableAccessibilityPlugin(boolean enableAccessibilityPlugin) {
			this.enableAccessibilityPlugin = enableAccessibilityPlugin;
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
	}

	public static class Root extends WailaConfig {
		public static final Codec<Root> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.fieldOf("enableProfiles").orElse(false).forGetter($ -> $.enableProfiles),
				Codec.intRange(0, 3).fieldOf("profileIndex").orElse(0).forGetter($ -> $.profileIndex),
				WailaConfig.MAP_CODEC.forGetter($ -> $),
				WailaConfig.History.CODEC.fieldOf("history")
						.orElseGet(() -> JadeCodecs.createFromEmptyMap(WailaConfig.History.CODEC))
						.forGetter($ -> $.history)
		).apply(i, WailaConfig.Root::new));

		private boolean enableProfiles;
		public int profileIndex;
		public History history;

		public Root(boolean enableProfiles, int profileIndex, WailaConfig config, History history) {
			super(config.name, config.general, config.overlay, config.formatting, config.accessibility, config.plugin);
			this.enableProfiles = enableProfiles;
			this.profileIndex = profileIndex;
			this.history = history;
		}

		public void setEnableProfiles(boolean enableProfiles) {
			this.enableProfiles = enableProfiles;
			if (!enableProfiles) {
				profileIndex = 0;
			}
		}

		public boolean isEnableProfiles() {
			return enableProfiles;
		}
	}

}
