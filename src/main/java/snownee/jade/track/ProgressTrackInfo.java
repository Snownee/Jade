package snownee.jade.track;

import snownee.jade.util.SmoothChasingValue;

public class ProgressTrackInfo extends TrackInfo {
	private final boolean canDecrease;
	private float width;
	private int ticksSinceWidthChanged;
	private float ticksSinceValueChanged;
	private final SmoothChasingValue smoothProgress = new SmoothChasingValue();
	private float progress;
	private float expectedWidth;

	public ProgressTrackInfo(boolean canDecrease, float progress, float width) {
		this.canDecrease = canDecrease;
		this.progress = progress;
		this.width = width;
		smoothProgress.start(progress);
	}

	public float getWidth() {
		return width;
	}

	@Override
	public void update(float pTicks) {
		if (progress != smoothProgress.getTarget() && ticksSinceValueChanged > 0) {
			if (ticksSinceValueChanged > 10) {
				smoothProgress.withSpeed(0.4F);
			} else if (canDecrease || progress > smoothProgress.getTarget()) {
				float spd = Math.abs(progress - smoothProgress.getTarget()) / ticksSinceValueChanged;
				spd = Math.max(0.1F, 4F * spd);
				smoothProgress.withSpeed(spd);
			}
			ticksSinceValueChanged = pTicks;
		} else {
			ticksSinceValueChanged += pTicks;
		}
		if (!canDecrease && progress < smoothProgress.getTarget()) {
			// start of a new loop
			if (smoothProgress.isMoving()) {
				smoothProgress.withSpeed(Math.max(0.5F, smoothProgress.getSpeed()));
				if (smoothProgress.getTarget() > 0.9F) {
					smoothProgress.target(1);
				}
			} else {
				smoothProgress.start(progress);
			}
		} else {
			smoothProgress.target(progress);
		}
		smoothProgress.tick(pTicks);
	}

	public void setExpectedWidth(float expectedWidth) {
		this.expectedWidth = expectedWidth;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public float getSmoothProgress() {
		return smoothProgress.value;
	}

	@Override
	public void tick() {
		if (width != expectedWidth) {
			if (expectedWidth > width || ++ticksSinceWidthChanged > 10) {
				width = expectedWidth;
				ticksSinceWidthChanged = 0;
			}
		}
	}
}
