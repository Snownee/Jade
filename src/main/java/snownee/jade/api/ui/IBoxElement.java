package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import snownee.jade.api.ITooltip;
import snownee.jade.impl.ui.StyledElement;

public interface IBoxElement extends IElement, StyledElement {

	ITooltip getTooltip();

	void setBoxProgress(MessageType type, float progress);

	float getBoxProgress();

	void clearBoxProgress();

	void setIcon(@Nullable IElement icon);

	int padding(ScreenDirection direction);

	void setPadding(ScreenDirection direction, int value);

	@Override
	BoxStyle getStyle();
}
