package snownee.jade.util;

import com.mojang.blaze3d.font.GlyphInfo;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.BitmapProvider;

public class JadeFont extends Font {
	public JadeFont(Font font) {
		super(font.provider);
		this.splitter = new StringSplitter((i, style) -> {
			GlyphInfo glyphInfo = getGlyphSource(style.getFont()).getGlyph(i).info();
			if (isFilteredGlyph(glyphInfo, lineHeight)) {
				return 0;
			}
			return glyphInfo.getAdvance(style.isBold());
		});
	}

	public static boolean isFilteredGlyph(GlyphInfo glyphInfo, int lineHeight) {
		if (glyphInfo instanceof BitmapProvider.Glyph glyph) {
			return glyph.height() * glyph.scale() > lineHeight + 4;
		}
		return glyphInfo.getAdvance() < -lineHeight;
	}
}
