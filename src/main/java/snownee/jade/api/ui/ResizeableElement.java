package snownee.jade.api.ui;

import org.jetbrains.annotations.Contract;

import com.google.common.base.Preconditions;

import snownee.jade.gui.ResizeableLayout;

public abstract class ResizeableElement extends Element implements ResizeableLayout {
	private int flexGrow;

	@Contract("_ -> this")
	public ResizeableElement flexGrow(int flexGrow) {
		setFlexGrow(flexGrow);
		return this;
	}

	@Override
	public void setFlexGrow(int flexGrow) {
		Preconditions.checkArgument(flexGrow >= 0, "flexGrow must be non-negative");
		this.flexGrow = flexGrow;
	}

	@Override
	public int getFlexGrow() {
		return flexGrow;
	}

	public void updateSize() {}
}
