package snownee.jade.gui.config;

import org.jspecify.annotations.Nullable;

import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionsList.Entry;

public class SecondaryPopup {

	public static final int FADE_MS = 100;
	public static final int SLIDE_DISTANCE = 8;

	private final OptionsList list;
	private @Nullable Entry expanded;
	private @Nullable Entry fading;
	private boolean sticky;
	private @Nullable Entry pendingEntry;
	private double pendingX;
	private double pendingY;
	private long pendingTime;
	private int x;
	private int y;
	private int width;
	private int height;
	private float alpha;
	private float slide;
	private long fadeTime = Util.getMillis();

	public SecondaryPopup(OptionsList list) {
		this.list = list;
	}

	public @Nullable Entry expanded() {
		return expanded;
	}

	public boolean sticky() {
		return sticky;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public float alpha() {
		return alpha;
	}

	public int slideOffset() {
		return Math.round((slide - 1) * SLIDE_DISTANCE);
	}

	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	public boolean isMouseOver(Entry entry, double mouseX, double mouseY) {
		if (entry.isMouseOver(mouseX, mouseY)) {
			return true;
		}
		if (width <= 0) {
			return false;
		}
		if (isInside(mouseX, mouseY)) {
			return true;
		}
		int cardLeft = entry.getX() - OptionsList.CARD_BLEED;
		int cardRight = entry.getX() + entry.getWidth() + OptionsList.CARD_BLEED;
		int cardTop = entry.getY() - OptionsList.CARD_BLEED;
		int cardBottom = entry.getY() + entry.getHeight() + OptionsList.CARD_BLEED;
		if (mouseX < Math.min(cardLeft, x) || mouseX >= Math.max(cardRight, x + width)) {
			return false;
		}
		return (mouseY >= cardBottom && mouseY < y) || (mouseY >= y + height && mouseY < cardTop);
	}

	public void setPending(Entry entry, double mouseX, double mouseY) {
		pendingEntry = entry;
		pendingX = mouseX;
		pendingY = mouseY;
		pendingTime = Util.getMillis();
	}

	public void clearPendingIfDragged(double mouseX, double mouseY) {
		if (pendingEntry != null && (Math.abs(mouseX - pendingX) > 2 || Math.abs(mouseY - pendingY) > 2)) {
			pendingEntry = null;
		}
	}

	public void commitPending() {
		if (pendingEntry == null || Util.getMillis() - pendingTime < 120) {
			return;
		}
		Entry entry = pendingEntry;
		pendingEntry = null;
		toggle(entry);
	}

	public void toggle(Entry entry) {
		if (expanded == entry) {
			collapse();
		} else {
			expand(entry, false);
			list.layoutCards();
		}
	}

	public void expand(Entry entry, boolean sticky) {
		expanded = entry;
		fading = null;
		alpha = 0;
		slide = 0;
		this.sticky = sticky;
	}

	public void collapse() {
		if (expanded != null) {
			fading = expanded;
			expanded = null;
			sticky = false;
			slide = 1;
			list.layoutCards();
		}
	}

	public void reset() {
		expanded = null;
		fading = null;
		sticky = false;
		pendingEntry = null;
		alpha = 0;
		slide = 0;
	}

	public void tick() {
		long now = Util.getMillis();
		float fadeMs = IWailaConfig.get().general().isDebug() ? FADE_MS * 5 : FADE_MS;
		float step = (now - fadeTime) / fadeMs;
		fadeTime = now;
		if (expanded != null) {
			alpha = Mth.clamp(alpha + step, 0, 1);
			slide = Mth.clamp(slide + step, 0, 1);
			fading = null;
		} else if (fading != null) {
			alpha = Mth.clamp(alpha - step, 0, 1);
			if (alpha <= 0) {
				fading = null;
				list.layoutCards();
			}
		}
	}

	public void applyAlpha() {
		Entry entry = effectiveEntry();
		if (entry == null) {
			return;
		}
		for (Entry option : entry.secondaryOptions()) {
			option.setPopupAlpha(alpha);
		}
	}

	@Nullable
	public Entry effectiveEntry() {
		if (expanded != null && list.children().contains(expanded)) {
			return expanded;
		}
		if (fading != null && list.children().contains(fading)) {
			return fading;
		}
		expanded = null;
		fading = null;
		return null;
	}
}
