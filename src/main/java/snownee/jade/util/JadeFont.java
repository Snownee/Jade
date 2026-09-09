package snownee.jade.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableFloat;

public class JadeFont extends Font {
	public JadeFont(Font font) {
		super(font.provider);
		this.splitter = font.splitter;
	}

	@Override
	public int width(String text) {
		return width(splitter.stringWidth(text), filteredWidth(s -> StringDecomposer.iterateFormatted(text, Style.EMPTY, s)));
	}

	@Override
	public int width(FormattedText text) {
		return width(splitter.stringWidth(text), filteredWidth(s -> StringDecomposer.iterateFormatted(text, Style.EMPTY, s)));
	}

	@Override
	public int width(FormattedCharSequence text) {
		return width(splitter.stringWidth(text), filteredWidth(text::accept));
	}

	private int width(float width, float filteredWidth) {
		return Math.max(0, Mth.ceil(width - filteredWidth));
	}

	private float filteredWidth(IteratingSink iterating) {
		MutableFloat width = new MutableFloat();
		iterating.accept((index, style, codePoint) -> {
			BakedGlyph glyph = getGlyphSource(style.getFont()).getGlyph(codePoint);
			if (isFilteredGlyph(glyph, lineHeight)) {
				width.add(glyph.info().getAdvance(style.isBold()));
			}
			return true;
		});
		return width.floatValue();
	}

	@FunctionalInterface
	private interface IteratingSink {
		void accept(FormattedCharSink sink);
	}

	public static boolean isFilteredGlyph(BakedGlyph glyph, int lineHeight) {
		if (glyph instanceof BakedSheetGlyph bakedSheetGlyph) {
			return bakedSheetGlyph.down - bakedSheetGlyph.up > lineHeight + 4;
		}
		return glyph.info().getAdvance() < -lineHeight;
	}
}
