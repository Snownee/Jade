package snownee.jade.util;

public class SmoothChasingValue {
	static final float eps = 1 / 4096f;
	float speed = 0.4f;
	float target = 0;

	public float value;

	protected float getCurrentDiff() {
		return getTarget() - value;
	}

	public float getTarget() {
		return target;
	}

	public boolean isMoving() {
		float diff = getCurrentDiff();
		return Math.abs(diff) > 1 / 128f;
	}

	public SmoothChasingValue set(float value) {
		this.value = value;
		return this;
	}

	public SmoothChasingValue start(float value) {
		this.value = value;
		target(value);
		return this;
	}

	public SmoothChasingValue target(float target) {
		this.target = target;
		return this;
	}

	public void tick(float pTicks) {
		float diff = getCurrentDiff();
		if (Math.abs(diff) < eps)
			return;
		set(value + diff * speed * pTicks);
	}

	public SmoothChasingValue withSpeed(float speed) {
		this.speed = speed;
		return this;
	}
}
