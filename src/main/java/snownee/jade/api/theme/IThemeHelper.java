package snownee.jade.api.theme;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import snownee.jade.JadeInternals;
import snownee.jade.api.ui.TextElement;

/**
 * Access to Jade's theme registry and theme-aware text helpers.
 */
public interface IThemeHelper {
	/**
	 * Returns the active theme helper instance.
	 *
	 * @return the active theme helper
	 */
	static IThemeHelper get() {
		return JadeInternals.getThemeHelper();
	}

	/**
	 * Returns the active theme.
	 *
	 * @return active theme
	 */
	Theme theme();

	/**
	 * Returns the normal text color for the active theme.
	 *
	 * @return normal text color
	 */
	default int getNormalColor() {
		return theme().text.colors().normal();
	}

	/**
	 * Returns all registered themes.
	 *
	 * @return registered themes
	 */
	Collection<Theme> getThemes();

	/**
	 * Returns the theme for the given identifier.
	 *
	 * @param id theme identifier
	 * @return matching theme
	 */
	Theme getTheme(Identifier id);

	/**
	 * Returns whether a theme with the given identifier exists.
	 *
	 * @param id theme identifier
	 * @return {@code true} if the theme exists
	 */
	boolean hasTheme(Identifier id);

	/**
	 * Styles the given text as informational.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent info(Object componentOrString);

	/**
	 * Styles the given text as a success message.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent success(Object componentOrString);

	/**
	 * Styles the given text as a warning.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent warning(Object componentOrString);

	/**
	 * Styles the given text as dangerous.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent danger(Object componentOrString);

	/**
	 * Styles the given text as a failure message.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent failure(Object componentOrString);

	/**
	 * Styles the given text as a title.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent title(Object componentOrString);

	/**
	 * Styles the given text with the mod name theme.
	 *
	 * @param componentOrString text to style
	 * @return styled component
	 */
	MutableComponent modName(Object componentOrString);

	/**
	 * Creates a mod-name text element.
	 *
	 * @param componentOrString text to style
	 * @return mod-name text element
	 */
	TextElement modNameElement(Object componentOrString);

	/**
	 * Formats a duration in seconds using the active theme.
	 *
	 * @param ticks duration in ticks
	 * @param tickRate ticks per second
	 * @return formatted duration
	 */
	default MutableComponent seconds(int ticks, float tickRate) {
		return seconds(ticks, tickRate, false);
	}

	/**
	 * Formats a duration in seconds using the active theme.
	 *
	 * @param ticks duration in ticks
	 * @param tickRate ticks per second
	 * @param alwaysOnePart whether to always show a single time part
	 * @return formatted duration
	 */
	MutableComponent seconds(int ticks, float tickRate, boolean alwaysOnePart);

	/**
	 * Returns whether the active theme uses a light color scheme.
	 *
	 * @return {@code true} if the theme uses a light color scheme
	 */
	default boolean isLightColorScheme() {
		return theme().lightColorScheme;
	}

	/**
	 * Returns the current theme generation.
	 *
	 * @return theme generation
	 */
	int generation();

	/**
	 * Overrides the active theme.
	 *
	 * @param theme theme override, or {@code null} to clear it
	 */
	void setThemeOverride(@Nullable Theme theme);
}
