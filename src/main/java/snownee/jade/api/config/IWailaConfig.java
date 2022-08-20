package snownee.jade.api.config;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;

@NonExtendable
public interface IWailaConfig {

	IConfigGeneral getGeneral();

	IConfigOverlay getOverlay();

	IConfigFormatting getFormatting();

	@NonExtendable
	public interface IConfigGeneral {

		void setDisplayTooltip(boolean displayTooltip);

		boolean getDisplayEntities();

		boolean getDisplayBlocks();

		void setDisplayBlocks(boolean displayBlocks);

		void setDisplayEntities(boolean displayEntities);

		void setDisplayMode(DisplayMode displayMode);

		void setHideFromDebug(boolean hideFromDebug);

		void toggleTTS();

		void setTTSMode(TTSMode ttsMode);

		void setMaxHealthForRender(int maxHealthForRender);

		void setMaxHeartsPerLine(int maxHeartsPerLine);

		void setDisplayFluids(boolean displayFluids);

		void setDisplayFluids(FluidMode displayFluids);

		void setItemModNameTooltip(boolean itemModNameTooltip);

		boolean shouldDisplayTooltip();

		DisplayMode getDisplayMode();

		boolean shouldHideFromDebug();

		boolean shouldEnableTextToSpeech();

		TTSMode getTTSMode();

		int getMaxHealthForRender();

		int getMaxHeartsPerLine();

		boolean shouldDisplayFluids();

		FluidMode getDisplayFluids();

		boolean showItemModNameTooltip();

		float getReachDistance();

		void setReachDistance(float reachDistance);

		void setDebug(boolean debug);

		boolean isDebug();
	}

	@NonExtendable
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

		boolean shouldShowIcon();

		void setIconMode(IconMode iconMode);

		IconMode getIconMode();
	}

	@NonExtendable
	public interface IConfigFormatting {

		void setModName(String modName);

		String getModName();

		Component registryName(String name);

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

	public enum FluidMode {
		NONE(ClipContext.Fluid.NONE), ANY(ClipContext.Fluid.ANY), SOURCE_ONLY(ClipContext.Fluid.SOURCE_ONLY);

		public final ClipContext.Fluid ctx;

		FluidMode(ClipContext.Fluid ctx) {
			this.ctx = ctx;
		}
	}
}