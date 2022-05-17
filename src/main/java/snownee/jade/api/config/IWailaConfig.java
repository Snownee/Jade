package snownee.jade.api.config;

import java.util.Collection;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;

public interface IWailaConfig {

	IConfigGeneral getGeneral();

	IConfigOverlay getOverlay();

	IConfigFormatting getFormatting();

	public interface IConfigGeneral {

		void setDisplayTooltip(boolean displayTooltip);

		boolean getDisplayEntities();

		boolean getDisplayBlocks();

		void setDisplayBlocks(boolean displayBlocks);

		void setDisplayEntities(boolean displayEntities);

		void setDisplayMode(DisplayMode displayMode);

		void setHideFromDebug(boolean hideFromDebug);

		void setIconMode(IconMode iconMode);

		void toggleTTS();

		void setTTSMode(TTSMode ttsMode);

		void setMaxHealthForRender(int maxHealthForRender);

		void setMaxHeartsPerLine(int maxHeartsPerLine);

		void setDisplayFluids(boolean displayFluids);

		void setDisplayFluids(ClipContext.Fluid displayFluids);

		boolean shouldDisplayTooltip();

		DisplayMode getDisplayMode();

		boolean shouldHideFromDebug();

		IconMode getIconMode();

		boolean shouldShowIcon();

		boolean shouldEnableTextToSpeech();

		TTSMode getTTSMode();

		int getMaxHealthForRender();

		int getMaxHeartsPerLine();

		boolean shouldDisplayFluids();

		ClipContext.Fluid getDisplayFluids();

		float getReachDistance();

		void setReachDistance(float reachDistance);

		void setDebug(boolean debug);

		boolean isDebug();
	}

	public interface IConfigOverlay {

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
	}

	public interface IConfigFormatting {

		void setModName(String modName);

		void setTitleName(String titleName);

		void setRegistryName(String registryName);

		String getModName();

		String getTitleName();

		String getRegistryName();

		Component title(Object title);
	}

	public enum IconMode {
		TOP, CENTERED, HIDE;
	}

	public enum TTSMode {
		TOGGLE, PRESS
	}

	public enum DisplayMode {
		HOLD_KEY, TOGGLE, LITE
	}
}