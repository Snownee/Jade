package snownee.jade.util;

import com.mojang.blaze3d.font.GlyphInfo;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.BitmapProvider;

public class JadeFont extends Font {
	public JadeFont(Font font) {
		super(font.fonts, font.filterFishyGlyphs);
		this.splitter = new StringSplitter((i, style) -> {
			GlyphInfo glyphInfo = getFontSet(style.getFont()).getGlyphInfo(i, filterFishyGlyphs);
			if (isTooLarge(glyphInfo, lineHeight)) {
				return 0;
			}
			return glyphInfo.getAdvance(style.isBold());
		});
	}

	public static boolean isTooLarge(GlyphInfo glyphInfo, int lineHeight) {
		return glyphInfo instanceof BitmapProvider.Glyph glyph && glyph.height() > lineHeight + 4;
	}
}
