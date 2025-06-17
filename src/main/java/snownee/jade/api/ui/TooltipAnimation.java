package snownee.jade.api.ui;

import java.util.function.BiFunction;

public class TooltipAnimation {
	public final Rect2f expectedRect = new Rect2f();
	public final Rect2f rect = new Rect2f();

	public long startTime = -1;
	public final Rect2f startRect = new Rect2f();
	public float scale = 1;
	public float showHideAlpha;
	public float alpha;

	public <R> R mapMousePosition(double mouseX, double mouseY, BiFunction<Double, Double, R> consumer) {
		mouseX = (mouseX - rect.getX()) / scale;
		mouseY = (mouseY - rect.getY()) / scale;
		return consumer.apply(mouseX, mouseY);
	}
}
