package snownee.jade.impl.gaze;

import org.joml.Vector2fc;
import org.jspecify.annotations.Nullable;

import snownee.jade.api.gaze.GazePoint;

public class StubGazePoint implements GazePoint {
	@Override
	public boolean canUse() {
		return false;
	}

	@Override
	public @Nullable Vector2fc pos() {
		throw new UnsupportedOperationException();
	}
}
