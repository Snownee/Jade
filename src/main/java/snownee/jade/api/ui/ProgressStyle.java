package snownee.jade.api.ui;

import java.util.Objects;

import org.jetbrains.annotations.Contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class ProgressStyle {

	protected boolean fitContentX = true;
	protected boolean fitContentY = true;
	protected ScreenDirection direction = ScreenDirection.RIGHT;

	@Contract("_ -> this")
	public abstract ProgressStyle textColor(int color);

	@Contract("_ -> this")
	public ProgressStyle direction(ScreenDirection direction) {
		this.direction = Objects.requireNonNull(direction);
		return this;
	}

	public ScreenDirection direction() {
		return direction;
	}

	@Contract("_ -> this")
	public ProgressStyle fitContentX(boolean fitContentX) {
		this.fitContentX = fitContentX;
		return this;
	}

	public boolean fitContentX() {
		return fitContentX;
	}

	@Contract("_ -> this")
	public ProgressStyle fitContentY(boolean fitContentY) {
		this.fitContentY = fitContentY;
		return this;
	}

	public boolean fitContentY() {
		return fitContentY;
	}

	public abstract void render(GuiGraphics guiGraphics, float x, float y, float w, float h, float progress, Component text);
}
