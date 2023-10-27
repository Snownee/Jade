package snownee.jade.impl.ui;

import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;

public interface StyledElement extends IElement {
	IElement getIcon();

	BoxStyle getStyle();
}
