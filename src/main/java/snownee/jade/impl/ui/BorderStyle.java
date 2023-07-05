package snownee.jade.impl.ui;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IBorderStyle;
import snownee.jade.api.ui.IBoxStyle;

@ScheduledForRemoval(inVersion = "1.20")
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

	public static IBoxStyle toBoxStyle(@Nullable IBorderStyle borderStyle) {
		if (borderStyle == null) {
			return IBoxStyle.Empty.INSTANCE;
		}
		BorderStyle style = (BorderStyle) borderStyle;
		BoxStyle box = new BoxStyle();
		box.borderColor = style.color;
		box.borderWidth = style.width;
		return box;
	}

}
