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
	static IThemeHelper get() {
		return JadeInternals.getThemeHelper();
	}

	Theme theme();

	default int getNormalColor() {
		return theme().text.colors().normal();
	}

	Collection<Theme> getThemes();

	Theme getTheme(Identifier id);

	boolean hasTheme(Identifier id);

	MutableComponent info(Object componentOrString);

	MutableComponent success(Object componentOrString);

	MutableComponent warning(Object componentOrString);

	MutableComponent danger(Object componentOrString);

	MutableComponent failure(Object componentOrString);

	MutableComponent title(Object componentOrString);

	MutableComponent modName(Object componentOrString);

	TextElement modNameElement(Object componentOrString);

	default MutableComponent seconds(int ticks, float tickRate) {
		return seconds(ticks, tickRate, false);
	}

	MutableComponent seconds(int ticks, float tickRate, boolean alwaysOnePart);

	default boolean isLightColorScheme() {
		return theme().lightColorScheme;
	}

	int generation();

	void setThemeOverride(@Nullable Theme theme);
}
