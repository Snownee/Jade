package snownee.jade.track;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import snownee.jade.api.view.ProgressView;
import snownee.jade.util.SmoothChasingValue;

public class ProgressTrackInfo extends TrackInfo {
	private final Int2ObjectMap<Child> children = new Int2ObjectOpenHashMap<>();
	private final boolean canDecrease;
	private int width;
	private int ticksSinceWidthChanged;
	private int expectedWidth;

	public ProgressTrackInfo(List<ProgressView.Part> parts, boolean canDecrease, int width) {
		this.canDecrease = canDecrease;
		this.width = width;
		setProgress(parts);
	}

	public int getWidth() {
		return width;
	}

	@Override
	public void update(float pTicks) {
		for (Child child : children.values()) {
			child.update(pTicks);
		}
	}

	public void setExpectedWidth(int expectedWidth) {
		this.expectedWidth = expectedWidth;
		if (expectedWidth > width) {
			width = expectedWidth;
			ticksSinceWidthChanged = 0;
		}
	}

	public void setProgress(List<ProgressView.Part> parts) {
		Set<Child> set = Sets.newHashSet();
		for (ProgressView.Part part : parts) {
			Child child = children.get(part.id());
			//noinspection ConstantValue
			if (child == null) {
				child = new Child(part, canDecrease);
				children.put(part.id(), child);
			} else {
				child.part = part;
				child.setProgress(child.part.progress());
			}
			set.add(child);
		}

		for (Child child : children.values()) {
			if (!set.contains(child)) {
				child.setProgress(0);
			}
		}
	}

	public float getSmoothProgress(ProgressView.Part part) {
		Child child = children.get(part.id());
		//noinspection ConstantValue
		if (child == null) {
			return part.progress();
		}
		return child.smoothProgress.value;
	}

	@Override
	public void tick() {
		if (expectedWidth < width && ++ticksSinceWidthChanged > 10) {
			width = expectedWidth;
			ticksSinceWidthChanged = 0;
		}
	}

	public static class Child {
		private ProgressView.Part part;
		private final boolean canDecrease;
		private final SmoothChasingValue smoothProgress = new SmoothChasingValue();
		private float ticksSinceValueChanged;

		public Child(ProgressView.Part part, boolean canDecrease) {
			this.canDecrease = canDecrease;
			this.part = part;
			smoothProgress.start(part.progress());
		}

		public void setProgress(float progress) {
			if (smoothProgress.getTarget() == progress) {
				return;
			}
			ticksSinceValueChanged = 0;
			if (canDecrease || progress > smoothProgress.getTarget()) {
				smoothProgress.target(progress);
			} else {
				smoothProgress.start(progress);
			}
		}

		public void update(float pTicks) {
			float progress = part.progress();
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
				if (smoothProgress.isMoving() && !part.targetDefined()) {
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
			if (part.targetDefined()) {
				float value = smoothProgress.value + pTicks * part.speed();
				if ((part.speed() > 0 && value >= part.target()) || (part.speed() < 0 && value <= part.target())) {
					value = part.target();
				}
				smoothProgress.set(value);
			} else {
				smoothProgress.tick(pTicks);
			}
		}
	}
}
