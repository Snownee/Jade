package snownee.jade.track;

public abstract class TrackInfo {
	protected boolean alive = true;
	protected boolean updatedThisTick;
	protected boolean persistent;

	public abstract void update(float pTicks);

	public abstract void tick();
}
