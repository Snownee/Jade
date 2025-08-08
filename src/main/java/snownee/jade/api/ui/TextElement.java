package snownee.jade.api.ui;

import org.jetbrains.annotations.Contract;

import com.mojang.brigadier.Message;

public abstract class TextElement extends ResizeableElement implements Message {
	@Contract("_ -> this")
	public abstract TextElement scale(float scale);

	@Contract("_ -> this")
	public abstract TextElement alpha(float alpha);
}
