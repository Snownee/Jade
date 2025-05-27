package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import snownee.jade.impl.Tooltip;
import snownee.jade.impl.ui.StyledElement;

public abstract class BoxElement extends Element implements StyledElement {
	public abstract Tooltip getTooltip();

	public abstract void setBoxProgress(MessageType type, float progress);

	public abstract float getBoxProgress();

	public abstract void clearBoxProgress();

	public abstract void setIcon(@Nullable Element icon);

	public abstract int padding(ScreenDirection direction);

	public abstract void setPadding(ScreenDirection direction, int value);
}
