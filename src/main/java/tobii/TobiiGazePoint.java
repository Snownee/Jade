package tobii;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import snownee.jade.Jade;
import snownee.jade.api.gaze.GazePoint;

public class TobiiGazePoint implements GazePoint {
	@Override
	public boolean canUse() {
		try {
			Tobii.loadIfNotLoaded();
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to load Tobii libraries", e);
			return false;
		}
		return Tobii.getTobiiStatus() == 0;
	}

	@Override
	public @Nullable Vector2fc pos() {
		float[] floats = Tobii.gazePosition();
		if (floats == null) {
			return null;
		}
		Window window = Minecraft.getInstance().getWindow();
		float gazeX = floats[0] * window.getGuiScaledWidth();
		float gazeY = floats[1] * window.getGuiScaledHeight();
		return new Vector2f(gazeX, gazeY);
	}
}
