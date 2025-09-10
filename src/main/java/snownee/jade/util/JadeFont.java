package snownee.jade.util;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;

public class JadeFont extends Font {
	public JadeFont(Font font) {
		super(font.provider);
		this.splitter = new StringSplitter((i, style) -> {
			BakedGlyph glyph = getGlyphSource(style.getFont()).getGlyph(i);
			if (isFilteredGlyph(glyph, lineHeight)) {
				return 0;
			}
			return glyph.info().getAdvance(style.isBold());
		});
	}

	public static boolean isFilteredGlyph(BakedGlyph glyph, int lineHeight) {
		if (glyph instanceof BakedSheetGlyph bakedSheetGlyph) {
			return bakedSheetGlyph.down - bakedSheetGlyph.up > lineHeight + 4;
		}
		return glyph.info().getAdvance() < -lineHeight;
	}
}
