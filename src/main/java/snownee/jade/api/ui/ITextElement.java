package snownee.jade.api.ui;

import org.jetbrains.annotations.Contract;

public interface ITextElement extends IElement {

	@Contract("_ -> this")
	ITextElement scale(float scale);

	@Contract("_ -> this")
	ITextElement zOffset(int zOffset);

	String getString();
}
