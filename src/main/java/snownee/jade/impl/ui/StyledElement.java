package snownee.jade.impl.ui;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;

public interface StyledElement {
	@Nullable Element getIcon();

	BoxStyle getStyle();
}
