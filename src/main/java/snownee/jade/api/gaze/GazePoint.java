package snownee.jade.api.gaze;

import org.joml.Vector2fc;
import org.jspecify.annotations.Nullable;

public interface GazePoint {
	boolean canUse();

	@Nullable Vector2fc pos();
}
