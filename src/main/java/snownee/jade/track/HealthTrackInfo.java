package snownee.jade.track;

public class HealthTrackInfo extends TrackInfo {
	private float health;
	private float lastHealth;
	private float absorption;
	private float lastAbsorption;
	private int ticksSinceHurt = 1000;

	public HealthTrackInfo(float health, float absorption) {
		this.health = lastHealth = health;
		this.absorption = lastAbsorption = absorption;
	}

	@Override
	public void update(float pTicks) {}

	@Override
	public void tick() {
		ticksSinceHurt++;
		if (health != lastHealth && ticksSinceHurt >= 5) {
			lastHealth = health;
		}
		if (absorption != lastAbsorption && ticksSinceHurt >= 5) {
			lastAbsorption = absorption;
		}
	}

	public float getLastHealth() {
		return lastHealth;
	}

	public float getLastAbsorption() {
		return lastAbsorption;
	}

	public boolean isBlinking() {
		return ticksSinceHurt < 5;
	}

	public void setHealth(float health, float absorption) {
		if (health < this.health) {
			this.ticksSinceHurt = 0;
		} else if (health > this.health) {
			this.lastHealth = health;
		}
		this.health = health;

		if (absorption < this.absorption) {
			this.ticksSinceHurt = 0;
		} else if (absorption > this.absorption) {
			this.lastAbsorption = absorption;
		}
		this.absorption = absorption;
	}
}
