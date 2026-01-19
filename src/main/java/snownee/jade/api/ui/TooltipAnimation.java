package snownee.jade.api.ui;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.input.MouseButtonEvent;

public class TooltipAnimation {
	public final Rect2f expectedRect = new Rect2f();
	public final Rect2f rect = new Rect2f();

	public long startTime = -1;
	public final Rect2f startRect = new Rect2f();
	public float scale = 1;
	public float showHideAlpha;
	public float alpha;

	public <R> @Nullable R mapMousePosition(double x, double y, BiFunction<Double, Double, @Nullable R> consumer) {
		x = (x - rect.getX()) / scale;
		y = (y - rect.getY()) / scale;
		return consumer.apply(x, y);
	}

	public <R> @Nullable R mapMousePosition(MouseButtonEvent event, Function<MouseButtonEvent, @Nullable R> consumer) {
		double x = (event.x() - rect.getX()) / scale;
		double y = (event.y() - rect.getY()) / scale;
		return consumer.apply(new MouseButtonEvent(x, y, event.buttonInfo()));
	}
}
