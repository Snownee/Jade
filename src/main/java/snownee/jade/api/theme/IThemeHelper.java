package snownee.jade.api.theme;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.Internals;

public interface IThemeHelper {
	static IThemeHelper get() {
		return Internals.getThemeHelper();
	}

	Theme theme();

	default int getNormalColor() {
		return theme().text.colors().normal();
	}

	Collection<Theme> getThemes();

	@NotNull
	Theme getTheme(ResourceLocation id);

	MutableComponent info(Object componentOrString);

	MutableComponent success(Object componentOrString);

	MutableComponent warning(Object componentOrString);

	MutableComponent danger(Object componentOrString);

	MutableComponent failure(Object componentOrString);

	MutableComponent title(Object componentOrString);

	MutableComponent modName(Object componentOrString);

	MutableComponent seconds(int ticks);

	default boolean isLightColorScheme() {
		return theme().lightColorScheme;
	}
}
