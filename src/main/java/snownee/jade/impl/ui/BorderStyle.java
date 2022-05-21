package snownee.jade.impl.ui;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import snownee.jade.api.ui.IBorderStyle;

@NonExtendable
public class BorderStyle implements IBorderStyle {

	public int width = 1;
	public int color = 0xFF808080;

	@Override
	public IBorderStyle width(int px) {
		width = px;
		return this;
	}

	@Override
	public IBorderStyle color(int color) {
		this.color = color;
		return this;
	}

}
