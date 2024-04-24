package snownee.jade.track;

public class HealthTrackInfo extends TrackInfo {
	private float health;
	private float lastHealth;
	private int ticksSinceHurt = 1000;

	public HealthTrackInfo(float health) {
		this.health = lastHealth = health;
	}

	@Override
	public void update(float pTicks) {}

	@Override
	public void tick() {
		ticksSinceHurt++;
		if (health != lastHealth && ticksSinceHurt >= 5) {
			lastHealth = health;
		}
	}

	public float getLastHealth() {
		return lastHealth;
	}

	public boolean isBlinking() {
		return ticksSinceHurt < 5;
	}

	public void setHealth(float health) {
		if (health < this.health) {
			this.ticksSinceHurt = 0;
		} else if (health > this.health) {
			this.lastHealth = health;
		}
		this.health = health;
	}
}
