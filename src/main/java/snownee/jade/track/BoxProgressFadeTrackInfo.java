package snownee.jade.track;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.ui.BoxProgress;

/**
 * Persistent box progress bar fade state, keyed by box tag in the {@link ProgressTracker}.
 */
public class BoxProgressFadeTrackInfo extends TrackInfo {
	public @Nullable BoxProgress progress;
	public float alpha;

	public BoxProgressFadeTrackInfo() {
		persistent = true;
	}

	/**
	 * Marks this track as in use for the current tick so the tracker keeps it alive.
	 */
	public void touch() {
		updatedThisTick = true;
	}

	@Override
	public void update(float pTicks) {
	}

	@Override
	public void tick() {
	}
}