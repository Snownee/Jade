package mcp.mobius.waila.impl.ui;

import java.awt.Color;

import mcp.mobius.waila.api.ui.IBorderStyle;

public class BorderStyle implements IBorderStyle {

	public int width = 1;
	public int color = Color.GRAY.getRGB();;

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
