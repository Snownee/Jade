package snownee.jade.impl.config;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.HumanoidArm;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
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
								  .forGetter(WailaConfig::getFormatting)
	).apply(i, WailaConfig::new));


	private final ConfigGeneral general;
	private final ConfigOverlay overlay;
	private final ConfigFormatting formatting;

	public WailaConfig(final ConfigGeneral general, final ConfigOverlay overlay, final ConfigFormatting formatting) {
		this.general = general;
		this.overlay = overlay;
		this.formatting = formatting;
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

	@Override
	public IPluginConfig getPlugin() {
		return PluginConfig.INSTANCE;
	}

	public static class ConfigGeneral implements IConfigGeneral {
		public static final Codec<ConfigGeneral> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.fieldOf("hintOverlayToggle").orElse(true).forGetter(it -> it.hintOverlayToggle),
				Codec.BOOL.fieldOf("hintNarratorToggle").orElse(true).forGetter(it -> it.hintNarratorToggle),
				Codec.BOOL.fieldOf("previewOverlay").orElse(true).forGetter($ -> $.previewOverlay),
				ConfigDisplay.CODEC.orElse(new ConfigDisplay()).forGetter($ -> $.configDisplay),
				Codec.BOOL.fieldOf("hideFromDebug").orElse(true).forGetter(ConfigGeneral::shouldHideFromDebug),
				Codec.BOOL.fieldOf("hideFromTabList").orElse(true).forGetter(ConfigGeneral::shouldHideFromTabList),
				Codec.BOOL.fieldOf("enableTextToSpeech").orElse(false).forGetter(ConfigGeneral::shouldEnableTextToSpeech),
				StringRepresentable.fromEnum(TTSMode::values)
								   .fieldOf("ttsMode").orElse(TTSMode.PRESS)
								   .forGetter(ConfigGeneral::getTTSMode),
				StringRepresentable.fromEnum(FluidMode::values)
								   .fieldOf("fluidMode").orElse(FluidMode.ANY)
								   .forGetter(ConfigGeneral::getDisplayFluids),
				Codec.floatRange(0, 20).fieldOf("reachDistance").orElse(0F).forGetter(ConfigGeneral::getReachDistance),
				Codec.BOOL.fieldOf("debug").orElse(false).forGetter(ConfigGeneral::isDebug),
				Codec.BOOL.fieldOf("itemModNameTooltip").orElse(true).forGetter(ConfigGeneral::showItemModNameTooltip),
				StringRepresentable.fromEnum(BossBarOverlapMode::values)
								   .fieldOf("bossBarOverlapMode").orElse(BossBarOverlapMode.PUSH_DOWN)
								   .forGetter(ConfigGeneral::getBossBarOverlapMode),
				Codec.BOOL.fieldOf("builtinCamouflage").orElse(true).forGetter(ConfigGeneral::getBuiltinCamouflage)
		).apply(i, ConfigGeneral::new));

		private static class ConfigDisplay {
			public static final MapCodec<ConfigDisplay> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
					Codec.BOOL.fieldOf("displayTooltip").orElse(true).forGetter(ConfigDisplay::shouldDisplayTooltip),
					Codec.BOOL.fieldOf("displayBlocks").orElse(true).forGetter(ConfigDisplay::getDisplayBlocks),
					Codec.BOOL.fieldOf("displayEntities").orElse(true).forGetter(ConfigDisplay::getDisplayEntities),
					Codec.BOOL.fieldOf("displayBosses").orElse(true).forGetter(ConfigDisplay::getDisplayBosses),
					StringRepresentable.fromEnum(DisplayMode::values)
									   .fieldOf("displayMode").orElse(DisplayMode.TOGGLE)
									   .forGetter(ConfigDisplay::getDisplayMode)
			).apply(i, ConfigDisplay::new));

			private boolean displayTooltip = true;
			private boolean displayBlocks = true;
			private boolean displayEntities = true;
			private boolean displayBosses = true;
			private DisplayMode displayMode = DisplayMode.TOGGLE;

			public ConfigDisplay(
					final boolean displayTooltip,
					final boolean displayBlocks,
					final boolean displayEntities,
					final boolean displayBosses,
					final DisplayMode displayMode
			) {
				this.displayTooltip = displayTooltip;
				this.displayBlocks = displayBlocks;
				this.displayEntities = displayEntities;
				this.displayBosses = displayBosses;
				this.displayMode = displayMode;
			}

			public ConfigDisplay() {
			}

			public boolean shouldDisplayTooltip() {
				return displayTooltip;
			}

			public void setDisplayTooltip(final boolean displayTooltip) {
				this.displayTooltip = displayTooltip;
			}

			public boolean getDisplayBlocks() {
				return displayBlocks;
			}

			public void setDisplayBlocks(final boolean displayBlocks) {
				this.displayBlocks = displayBlocks;
			}

			public boolean getDisplayEntities() {
				return displayEntities;
			}

			public void setDisplayEntities(final boolean displayEntities) {
				this.displayEntities = displayEntities;
			}

			public boolean getDisplayBosses() {
				return displayBosses;
			}

			public void setDisplayBosses(final boolean displayBosses) {
				this.displayBosses = displayBosses;
			}

			public DisplayMode getDisplayMode() {
				return displayMode;
			}

			public void setDisplayMode(final DisplayMode displayMode) {
				this.displayMode = displayMode;
			}
		}


		public static final List<String> itemModNameTooltipDisabledByMods = Lists.newArrayList("emi");
		public boolean hintOverlayToggle = true;
		public boolean hintNarratorToggle = true;
		public boolean previewOverlay = true;
		private ConfigDisplay configDisplay = new ConfigDisplay();
		private boolean hideFromDebug = true;
		private boolean hideFromTabList = true;
		private boolean enableTextToSpeech = false;
		private TTSMode ttsMode = TTSMode.PRESS;
		private FluidMode fluidMode = FluidMode.ANY;
		private float reachDistance = 0;
		@Expose
		private boolean debug = false;
		private boolean itemModNameTooltip = true;
		private BossBarOverlapMode bossBarOverlapMode = BossBarOverlapMode.PUSH_DOWN;
		private boolean builtinCamouflage = true;

		public ConfigGeneral(
				final boolean hintOverlayToggle,
				final boolean hintNarratorToggle,
				final boolean previewOverlay,
				ConfigDisplay configDisplay,
				final boolean hideFromDebug,
				final boolean hideFromTabList,
				final boolean enableTextToSpeech,
				final TTSMode ttsMode,
				final FluidMode fluidMode,
				final float reachDistance,
				final boolean debug,
				final boolean itemModNameTooltip,
				final BossBarOverlapMode bossBarOverlapMode,
				final boolean builtinCamouflage
		) {
			this.hintOverlayToggle = hintOverlayToggle;
			this.hintNarratorToggle = hintNarratorToggle;
			this.previewOverlay = previewOverlay;
			this.configDisplay = configDisplay;
			this.hideFromDebug = hideFromDebug;
			this.hideFromTabList = hideFromTabList;
			this.enableTextToSpeech = enableTextToSpeech;
			this.ttsMode = ttsMode;
			this.fluidMode = fluidMode;
			this.reachDistance = reachDistance;
			this.debug = debug;
			this.itemModNameTooltip = itemModNameTooltip;
			this.bossBarOverlapMode = bossBarOverlapMode;
			this.builtinCamouflage = builtinCamouflage;
		}

		public ConfigGeneral() {
		}

		public static void init() {
			/* off */
			List<String> names = itemModNameTooltipDisabledByMods.stream()
					.filter(CommonProxy::isModLoaded)
					.map(ModIdentification::getModName)
					.toList();
			/* on */
			itemModNameTooltipDisabledByMods.clear();
			itemModNameTooltipDisabledByMods.addAll(names);
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
		public boolean shouldDisplayTooltip() {return configDisplay.shouldDisplayTooltip();}

		@Override
		public void setDisplayTooltip(final boolean displayTooltip) {configDisplay.setDisplayTooltip(displayTooltip);}

		@Override
		public boolean getDisplayBlocks() {return configDisplay.getDisplayBlocks();}

		@Override
		public void setDisplayBlocks(final boolean displayBlocks) {configDisplay.setDisplayBlocks(displayBlocks);}

		@Override
		public boolean getDisplayEntities() {return configDisplay.getDisplayEntities();}

		@Override
		public void setDisplayEntities(final boolean displayEntities) {configDisplay.setDisplayEntities(displayEntities);}

		@Override
		public boolean getDisplayBosses() {return configDisplay.getDisplayBosses();}

		@Override
		public void setDisplayBosses(final boolean displayBosses) {configDisplay.setDisplayBosses(displayBosses);}

		@Override
		public DisplayMode getDisplayMode() {return configDisplay.getDisplayMode();}

		@Override
		public void setDisplayMode(final DisplayMode displayMode) {configDisplay.setDisplayMode(displayMode);}

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
		public float getReachDistance() {
			return reachDistance;
		}

		@Override
		public void setReachDistance(float reachDistance) {
			this.reachDistance = Mth.clamp(reachDistance, 0, 20);
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
		public boolean getBuiltinCamouflage() {
			return builtinCamouflage;
		}

		@Override
		public void setBuiltinCamouflage(boolean builtinCamouflage) {
			this.builtinCamouflage = builtinCamouflage;
		}

	}

	public static class ConfigOverlay implements IConfigOverlay {
		public static final Codec<ConfigOverlay> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.fieldOf("activeTheme").orElse(Theme.DARK.id).forGetter($ -> $.activeTheme),
				Codec.INT.optionalFieldOf("themesHash", 0).forGetter($ -> $.themesHash),
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


		public ResourceLocation activeTheme = Theme.DARK.id;
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

		public ConfigOverlay() {
		}

		public ConfigOverlay(
				final ResourceLocation activeTheme,
				final int themesHash,
				final float overlayPosX,
				final float overlayPosY,
				final float overlayScale,
				final float overlayAnchorX,
				final float overlayAnchorY,
				final boolean overlaySquare,
				final boolean flipMainHand,
				final float autoScaleThreshold,
				final float alpha,
				final IconMode iconMode,
				final boolean animation,
				final float disappearingDelay
		) {
			this.activeTheme = activeTheme;
			this.themesHash = themesHash;
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
		@Deprecated
		public Collection<Theme> getThemes() {
			return IThemeHelper.get().getThemes();
		}

		@Override
		public void applyTheme(ResourceLocation id) {
			activeThemeInstance = IThemeHelper.get().getTheme(id);
			activeTheme = activeThemeInstance.id;
			BoxStyle.DEFAULT.borderColor = activeThemeInstance.boxBorderColor;
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
		public void setDisappearingDelay(float delay) {
			disappearingDelay = delay;
		}

		@Override
		public float getDisappearingDelay() {
			return disappearingDelay;
		}

	}

	public static class ConfigFormatting implements IConfigFormatting {
		public static final Codec<ConfigFormatting> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("modName")
									  .orElse("§9§o%s")
									  .forGetter(ConfigFormatting::getModName)
		).apply(i, ConfigFormatting::new));

		private String modName = "§9§o%s";

		public ConfigFormatting(final String modName) {
			this.modName = modName;
		}

		public ConfigFormatting() {
		}

		@Override
		public String getModName() {
			return modName;
		}

		@Override
		public void setModName(String modName) {
			this.modName = modName;
		}

		@Override
		@Deprecated
		public Component title(Object title) {
			return IThemeHelper.get().title(title);
		}

		@Override
		public Component registryName(String name) {
			return Component.literal(name).withStyle(IThemeHelper.get().isLightColorScheme() ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY);
		}
	}

}
