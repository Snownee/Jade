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

	public abstract float getBoxProgress();

	public abstract void clearBoxProgress();

	public abstract void setIcon(@Nullable Element icon);
}
