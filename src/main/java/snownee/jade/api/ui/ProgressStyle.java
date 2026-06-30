package snownee.jade.api.ui;

import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Rendering strategy for a progress bar.
 */
public abstract class ProgressStyle {

	protected boolean fitContentX = true;
	protected boolean fitContentY = true;
	protected ScreenDirection direction = ScreenDirection.RIGHT;
	protected @Nullable Identifier foreground;
	protected boolean canDecrease;

	/**
	 * Sets the fill direction.
	 *
	 * @param direction progress direction
	 * @return this style
	 */
	@Contract("_ -> this")
	public ProgressStyle direction(ScreenDirection direction) {
		this.direction = Objects.requireNonNull(direction);
		return this;
	}

	/**
	 * Returns the fill direction.
	 *
	 * @return fill direction
	 */
	public ScreenDirection direction() {
		return direction;
	}

	/**
	 * Sets whether the width should adapt to the content.
	 *
	 * @param fitContentX {@code true} to fit width
	 * @return this style
	 */
	@Contract("_ -> this")
	public ProgressStyle fitContentX(boolean fitContentX) {
		this.fitContentX = fitContentX;
		return this;
	}

	/**
	 * Returns whether the width should adapt to the content.
	 *
	 * @return {@code true} if the width fits content
	 */
	public boolean fitContentX() {
		return fitContentX;
	}

	/**
	 * Sets whether the height should adapt to the content.
	 *
	 * @param fitContentY {@code true} to fit height
	 * @return this style
	 */
	@Contract("_ -> this")
	public ProgressStyle fitContentY(boolean fitContentY) {
		this.fitContentY = fitContentY;
		return this;
	}

	/**
	 * Returns whether the height should adapt to the content.
	 *
	 * @return {@code true} if the height fits content
	 */
	public boolean fitContentY() {
		return fitContentY;
	}

	/**
	 * Sets the foreground sprite.
	 *
	 * @param foreground foreground sprite
	 * @return this style
	 */
	@Contract("_ -> this")
	public ProgressStyle foreground(@Nullable Identifier foreground) {
		this.foreground = foreground;
		return this;
	}

	/**
	 * Returns the foreground sprite.
	 *
	 * @return foreground sprite or {@code null}
	 */
	public @Nullable Identifier foreground() {
		return foreground;
	}

	/**
	 * Sets whether the bar may decrease.
	 *
	 * @param canDecrease {@code true} if the bar can move backwards
	 * @return this style
	 */
	@Contract("_ -> this")
	public ProgressStyle canDecrease(boolean canDecrease) {
		this.canDecrease = canDecrease;
		return this;
	}

	/**
	 * Returns whether the bar may decrease.
	 *
	 * @return {@code true} if the bar can move backwards
	 */
	public boolean canDecrease() {
		return canDecrease;
	}

	/**
	 * Renders the progress bar.
	 *
	 * @param guiGraphics graphics context
	 * @param x x position
	 * @param y y position
	 * @param w width
	 * @param h height
	 * @param progress progress value in the {@code 0..1} range
	 * @param text optional label
	 */
	public abstract void render(GuiGraphicsExtractor guiGraphics, float x, float y, float w, float h, float progress, Component text);
}
