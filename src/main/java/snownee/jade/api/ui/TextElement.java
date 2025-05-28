package snownee.jade.api.ui;

import org.jetbrains.annotations.Contract;

public abstract class TextElement extends ResizeableElement {
	@Contract("_ -> this")
	public abstract TextElement scale(float scale);

	public abstract String getString();
}
