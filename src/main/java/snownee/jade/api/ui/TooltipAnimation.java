package snownee.jade.api.ui;

public class TooltipAnimation {
	public final Rect2f expectedRect = new Rect2f(0, 0, 0, 0);
	public final Rect2f rect = new Rect2f(0, 0, 0, 0); //TODO rename to roundedRect

	public long startTime = -1;
	public final Rect2f startRect = new Rect2f(0, 0, 0, 0);
	//	public final Rect2f rect = new Rect2f(0, 0, 0, 0);
	public float scale = 1;
	public float alpha;
}
