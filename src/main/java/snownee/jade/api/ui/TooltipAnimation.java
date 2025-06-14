package snownee.jade.api.ui;

public class TooltipAnimation {
	public final Rect2f expectedRect = new Rect2f();
	public final Rect2f rect = new Rect2f();

	public long startTime = -1;
	public final Rect2f startRect = new Rect2f();
	public float scale = 1;
	public float showHideAlpha;
	public float alpha;
}
