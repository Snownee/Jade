package snownee.jade.api.config;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ClipContext;
import snownee.jade.JadeInternals;
import snownee.jade.api.SimpleStringRepresentable;
import snownee.jade.api.theme.Theme;

/**
 * Access to Jade's global client configuration.
 */
@NonExtendable
public interface IWailaConfig {

	/**
	 * Returns the active configuration instance.
	 *
	 * @return the current config
	 */
	static IWailaConfig get() {
		return JadeInternals.getWailaConfig();
	}

	/**
	 * Returns the general settings section.
	 *
	 * @return general settings
	 */
	General general();

	/**
	 * Returns the overlay settings section.
	 *
	 * @return overlay settings
	 */
	Overlay overlay();

	/**
	 * Returns the formatting settings section.
	 *
	 * @return formatting settings
	 */
	Formatting formatting();

	/**
	 * Returns the accessibility settings section.
	 *
	 * @return accessibility settings
	 */
	Accessibility accessibility();

	/**
	 * Returns the plugin configuration view.
	 *
	 * @return plugin configuration
	 */
	IPluginConfig plugin();

	/**
	 * Persists the current configuration state.
	 */
	void save();

	/**
	 * Invalidates cached config state.
	 */
	void invalidate();

	/**
	 * Returns the configuration name.
	 *
	 * @return the profile name
	 */
	String getName();

	/**
	 * Updates the configuration name.
	 *
	 * @param name new profile name
	 */
	void setName(String name);

	/**
	 * Controls icon placement inside the overlay.
	 */
	enum IconMode implements SimpleStringRepresentable {
		TOP, CENTERED, INLINE, HIDE
	}

	/**
	 * Controls the text-to-speech activation mode.
	 */
	enum TTSMode implements SimpleStringRepresentable {
		TOGGLE, PRESS
	}

	/**
	 * Controls how much of the overlay remains visible.
	 */
	enum DisplayMode implements SimpleStringRepresentable {
		HOLD_KEY, TOGGLE, LITE
	}

	/**
	 * Controls which fluid clipping mode Jade uses.
	 */
	enum FluidMode implements SimpleStringRepresentable {
		NONE(ClipContext.Fluid.NONE),
		ANY(ClipContext.Fluid.ANY),
		FALLBACK(ClipContext.Fluid.NONE);

		public final ClipContext.Fluid ctx;

		FluidMode(ClipContext.Fluid ctx) {
			this.ctx = ctx;
		}
	}

	/**
	 * Controls how Jade interacts with the boss bar.
	 */
	enum BossBarOverlapMode implements SimpleStringRepresentable {
		NO_OPERATION, HIDE_BOSS_BAR, HIDE_TOOLTIP, PUSH_DOWN
	}

	/**
	 * Controls which perspective the overlay should follow.
	 */
	enum PerspectiveMode implements SimpleStringRepresentable {
		CAMERA, EYE
	}

	/**
	 * Controls how storage handlers are rendered.
	 */
	enum HandlerDisplayStyle implements SimpleStringRepresentable {
		PROGRESS_BAR, ICON, PLAIN_TEXT
	}

	@NonExtendable
	interface General {

		void setDisplayTooltip(boolean displayTooltip);

		boolean getDisplayEntities();

		void setDisplayEntities(boolean displayEntities);

		boolean getDisplayBosses();

		void setDisplayBosses(boolean displayBosses);

		boolean getDisplayBlocks();

		void setDisplayBlocks(boolean displayBlocks);

		void setHideFromTabList(boolean hideFromTabList);

		void setHideFromGUIs(boolean hideFromGUIs);

		void setItemModNameTooltip(boolean itemModNameTooltip);

		boolean shouldDisplayTooltip();

		DisplayMode getDisplayMode();

		void setDisplayMode(DisplayMode displayMode);

		boolean shouldHideFromTabList();

		boolean shouldHideFromGUIs();

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

		PerspectiveMode getPerspectiveMode();

		void setPerspectiveMode(PerspectiveMode perspectiveMode);
	}

	@NonExtendable
	interface Overlay {

		static int applyAlpha(int color, float alpha) {
			if (alpha == 1) {
				return color;
			}
			if (alpha == 0) {
				return 0;
			}
			int prevAlpha = ARGB.alpha(color);
			if (prevAlpha != 255) {
				alpha *= prevAlpha / 255F;
			}
			return ARGB.color(ARGB.as8BitChannel(alpha), color);
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

		float getAutoScaleThreshold();

		float getAlpha();

		void setAlpha(float alpha);

		Theme getTheme();

		void applyTheme(Identifier id);

		boolean shouldShowIcon();

		IconMode getIconMode();

		void setIconMode(IconMode iconMode);

		boolean getAnimation();

		void setAnimation(boolean animation);

		float getDisappearingDelay();

		void setDisappearingDelay(float delay);
	}

	@NonExtendable
	interface Formatting {

		Style getItemModNameStyle();

		void setItemModNameStyle(Style itemModNameStyle);

		Component registryName(String name);
	}

	@NonExtendable
	interface Accessibility {

		boolean shouldEnableTextToSpeech();

		void toggleTTS();

		TTSMode getTTSMode();

		void setTTSMode(TTSMode ttsMode);

		boolean getEnableAccessibilityPlugin();

		void setEnableAccessibilityPlugin(boolean showAccessibilityPlugins);

		boolean getFlipMainHand();

		void setFlipMainHand(boolean overlaySquare);

		float tryFlip(float f);

		void setTextBackgroundOpacity(float opacity);

		float getTextBackgroundOpacity();

		void setNarrateKeys(boolean narrateKeys);

		boolean getNarrateKeys();
	}
}
