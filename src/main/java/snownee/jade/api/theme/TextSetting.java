package snownee.jade.api.theme;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Style;
import snownee.jade.api.ui.ColorPalette;

public record TextSetting(ColorPalette colors, boolean shadow, @Nullable Style modNameStyle, int itemAmountColor) {
	public static final TextSetting DEFAULT = new TextSetting(ColorPalette.DEFAULT, true, Optional.empty(), 0xFFFFFFFF);

	public TextSetting(ColorPalette colors, boolean shadow, Optional<Style> modNameStyle, int itemAmountColor) {
		this(colors, shadow, modNameStyle.orElse(null), itemAmountColor);
	}
}
