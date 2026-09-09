package snownee.jade.api.ui;

import org.jspecify.annotations.Nullable;

/**
 * Desired state of a box progress bar for a single frame.
 *
 * @param progress normalized progress in the range {@code [0, 1]}; values outside the range are clamped when rendered
 * @param type semantic color; resolved through the box style's {@code boxProgressColors} palette
 * @param color optional opaque base color; when non-null it overrides the color derived from {@code type}
 * @param alpha transparency multiplier in the range {@code [0, 1]}; applied to the resolved color by the box
 */
public record BoxProgress(float progress, MessageType type, @Nullable Integer color, float alpha) {

	public BoxProgress(float progress, MessageType type) {
		this(progress, type, null, 1);
	}

	public BoxProgress(float progress, MessageType type, float alpha) {
		this(progress, type, null, alpha);
	}

}