package snownee.jade.api.ui;

import org.jspecify.annotations.Nullable;

import snownee.jade.impl.Tooltip;
import snownee.jade.impl.ui.StyledElement;

/**
 * Container element that wraps an inner tooltip.
 */
public abstract class BoxElement extends ResizeableElement implements StyledElement {
	public abstract Tooltip getTooltip();

	public abstract void setBoxProgress(MessageType type, float progress);

	public abstract void clearBoxProgress();

	/**
	 * Attaches a progress bar provider. Providers are queried every frame in ascending priority order; the first
	 * non-null result wins.
	 *
	 * @param priority lower values are queried first
	 * @param provider per-frame progress bar provider
	 */
	public abstract void addProgressProvider(int priority, IBoxProgressProvider provider);

	public void addProgressProvider(IBoxProgressProvider provider) {
		addProgressProvider(0, provider);
	}

	public abstract void setIcon(@Nullable Element icon);
}
