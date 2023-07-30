package snownee.jade.api.config;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.network.chat.Component;
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

	@NonExtendable
	interface IConfigGeneral {

		void setDisplayTooltip(boolean displayTooltip);

		boolean getDisplayEntities();

		boolean getDisplayBosses();

		boolean getDisplayBlocks();

		void setDisplayBlocks(boolean displayBlocks);

		void setDisplayEntities(boolean displayEntities);

		void setDisplayBosses(boolean displayBosses);

		void setDisplayMode(DisplayMode displayMode);

		void setHideFromDebug(boolean hideFromDebug);

		void setHideFromTabList(boolean hideFromTabList);

		void toggleTTS();

		void setTTSMode(TTSMode ttsMode);

		void setDisplayFluids(boolean displayFluids);

		void setDisplayFluids(FluidMode displayFluids);

		void setItemModNameTooltip(boolean itemModNameTooltip);

		boolean shouldDisplayTooltip();

		DisplayMode getDisplayMode();

		boolean shouldHideFromDebug();

		boolean shouldHideFromTabList();

		boolean shouldEnableTextToSpeech();

		TTSMode getTTSMode();

		boolean shouldDisplayFluids();

		FluidMode getDisplayFluids();

		boolean showItemModNameTooltip();

		float getReachDistance();

		void setReachDistance(float reachDistance);

		BossBarOverlapMode getBossBarOverlapMode();

		void setBossBarOverlapMode(BossBarOverlapMode mode);

		void setDebug(boolean debug);

		boolean isDebug();
	}

	@NonExtendable
	interface IConfigOverlay {

		void setOverlayPosX(float overlayPosX);

		void setOverlayPosY(float overlayPosY);

		void setOverlayScale(float overlayScale);

		void setAnchorX(float overlayAnchorX);

		void setAnchorY(float overlayAnchorY);

		float getOverlayPosX();

		float getOverlayPosY();

		float getOverlayScale();

		float getAnchorX();

		float getAnchorY();

		void setFlipMainHand(boolean overlaySquare);

		boolean getFlipMainHand();

		float tryFlip(float f);

		void setSquare(boolean overlaySquare);

		boolean getSquare();

		float getAutoScaleThreshold();

		float getAlpha();

		Theme getTheme();

		Collection<Theme> getThemes();

		void setAlpha(float alpha);

		void applyTheme(ResourceLocation id);

		static int applyAlpha(int color, float alpha) {
			int prevAlphaChannel = (color >> 24) & 0xFF;
			if (prevAlphaChannel > 0)
				alpha *= prevAlphaChannel / 256f;
			int alphaChannel = (int) (0xFF * Mth.clamp(alpha, 0, 1));
			return (color & 0xFFFFFF) | alphaChannel << 24;
		}

		boolean shouldShowIcon();

		void setIconMode(IconMode iconMode);

		IconMode getIconMode();

		void setAnimation(boolean animation);

		boolean getAnimation();
	}

	@NonExtendable
	interface IConfigFormatting {

		void setModName(String modName);

		String getModName();

		Component registryName(String name);

		@Deprecated
		Component title(Object title);
	}

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
}
