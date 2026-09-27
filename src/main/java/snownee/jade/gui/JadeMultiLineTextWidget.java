package snownee.jade.gui;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.Util;

/**
 * A {@link StringWidget} variant that, when limited to more than one row, wraps its text into at most
 * {@code maxRows} rows via {@link MultiLineLabel}, clipping the last row with an ellipsis when the text overflows.
 * With {@code maxRows <= 1} it behaves exactly like {@code StringWidget}.
 */
public class JadeMultiLineTextWidget extends StringWidget {

	private int maxRows = 1;
	private int trackedMaxWidth;
	private final SingleKeyCache<CacheKey, MultiLineLabel> cache;

	public JadeMultiLineTextWidget(Component message, Font font) {
		super(message, font);
		cache = createCache();
	}

	public JadeMultiLineTextWidget(int width, int height, Component message, Font font) {
		super(width, height, message, font);
		cache = createCache();
	}

	public JadeMultiLineTextWidget(int x, int y, int width, int height, Component message, Font font) {
		super(x, y, width, height, message, font);
		cache = createCache();
	}

	private SingleKeyCache<CacheKey, MultiLineLabel> createCache() {
		return Util.singleKeyCache(key -> MultiLineLabel.create(getFont(), key.maxWidth(), key.maxRows(), key.message()));
	}

	public JadeMultiLineTextWidget setMaxRows(int maxRows) {
		this.maxRows = Math.max(1, maxRows);
		return this;
	}

	public int getMaxRows() {
		return maxRows;
	}

	@Override
	public StringWidget setMaxWidth(int maxWidth, TextOverflow textOverflow) {
		trackedMaxWidth = maxWidth;
		return super.setMaxWidth(maxWidth, textOverflow);
	}

	@Override
	public void visitLines(ActiveTextCollector output) {
		if (maxRows <= 1) {
			super.visitLines(output);
			return;
		}
		MultiLineLabel label = label();
		int lineHeight = getFont().lineHeight;
		int totalHeight = label.getLineCount() * lineHeight;
		int top = getY() + (getHeight() - totalHeight) / 2;
		label.visitLines(TextAlignment.LEFT, getX(), top, lineHeight, output);
	}

	private MultiLineLabel label() {
		int maxWidth = trackedMaxWidth > 0 ? trackedMaxWidth : Integer.MAX_VALUE;
		return cache.getValue(new CacheKey(getMessage(), maxWidth, maxRows));
	}

	private record CacheKey(Component message, int maxWidth, int maxRows) {
	}
}
