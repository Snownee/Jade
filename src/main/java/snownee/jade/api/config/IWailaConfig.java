package snownee.jade.api.config;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import snownee.jade.Internals;
import snownee.jade.api.theme.Theme;

@NonExtendable
public interface IWailaConfig {

	static IWailaConfig get() {
		return Internals.getWailaConfig();
	}

	IConfigGeneral getGeneral();

	IConfigOverlay getOverlay();

	IConfigFormatting getFormatting();

	IPluginConfig getPlugin();

	enum IconMode {
		TOP, CENTERED, HIDE
	}

	enum TTSMode {
		TOGGLE, PRESS
	}

	enum DisplayMode {
		HOLD_KEY, TOGGLE, LITE
	}

	enum FluidMode {
		NONE(ClipContext.Fluid.NONE), ANY(ClipContext.Fluid.ANY), SOURCE_ONLY(ClipContext.Fluid.SOURCE_ONLY);

		public final ClipContext.Fluid ctx;

		FluidMode(ClipContext.Fluid ctx) {
			this.ctx = ctx;
		}
	}

	enum BossBarOverlapMode {
		NO_OPERATION, HIDE_BOSS_BAR, HIDE_TOOLTIP, PUSH_DOWN
	}

	@NonExtendable
	interface IConfigGeneral {

		void setDisplayTooltip(boolean displayTooltip);

		boolean getDisplayEntities();

		void setDisplayEntities(boolean displayEntities);

		boolean getDisplayBosses();

		void setDisplayBosses(boolean displayBosses);

		boolean getDisplayBlocks();

		void setDisplayBlocks(boolean displayBlocks);

		void setHideFromDebug(boolean hideFromDebug);

		void setHideFromTabList(boolean hideFromTabList);

		void toggleTTS();

		void setItemModNameTooltip(boolean itemModNameTooltip);

		boolean shouldDisplayTooltip();

		DisplayMode getDisplayMode();

		void setDisplayMode(DisplayMode displayMode);

		boolean shouldHideFromDebug();

		boolean shouldHideFromTabList();

		boolean shouldEnableTextToSpeech();

		TTSMode getTTSMode();

		void setTTSMode(TTSMode ttsMode);

		boolean shouldDisplayFluids();

		FluidMode getDisplayFluids();

		void setDisplayFluids(boolean displayFluids);

		void setDisplayFluids(FluidMode displayFluids);

		boolean showItemModNameTooltip();

		float getExtendedReach();

		void setExtendedReach(float extendedReach);

		BossBarOverlapMode getBossBarOverlapMode();

		void setBossBarOverlapMode(BossBarOverlapMode mode);

		boolean isDebug();

		void setDebug(boolean debug);

		boolean getBuiltinCamouflage();

		void setBuiltinCamouflage(boolean builtinCamouflage);
	}

	@NonExtendable
	interface IConfigOverlay {

		static int applyAlpha(int color, float alpha) {
			int prevAlphaChannel = (color >> 24) & 0xFF;
			if (prevAlphaChannel > 0) {
				alpha *= prevAlphaChannel / 256f;
			}
			int alphaChannel = Mth.clamp((int) (0xFF * alpha), 4, 255);
			return (color & 0xFFFFFF) | alphaChannel << 24;
		}

		float getOverlayPosX();

		void setOverlayPosX(float overlayPosX);

		float getOverlayPosY();

		void setOverlayPosY(float overlayPosY);

		float getOverlayScale();

		void setOverlayScale(float overlayScale);

		float getAnchorX();

		void setAnchorX(float overlayAnchorX);

		float getAnchorY();

		void setAnchorY(float overlayAnchorY);

		boolean getFlipMainHand();

		void setFlipMainHand(boolean overlaySquare);

		float tryFlip(float f);

		boolean getSquare();

		void setSquare(boolean overlaySquare);

		float getAutoScaleThreshold();

		float getAlpha();

		void setAlpha(float alpha);

		Theme getTheme();

		void applyTheme(ResourceLocation id);

		boolean shouldShowIcon();

		IconMode getIconMode();

		void setIconMode(IconMode iconMode);

		boolean getAnimation();

		void setAnimation(boolean animation);

		float getDisappearingDelay();

		void setDisappearingDelay(float delay);
	}

	@NonExtendable
	interface IConfigFormatting {

		Style getItemModNameStyle();

		void setItemModNameStyle(Style itemModNameStyle);

		Component registryName(String name);
	}
}
