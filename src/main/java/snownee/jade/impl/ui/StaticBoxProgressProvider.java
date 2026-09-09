package snownee.jade.impl.ui;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.JadeClient;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxProgress;
import snownee.jade.api.ui.IBoxProgressProvider;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.view.ProgressView;
import snownee.jade.track.ProgressTrackInfo;

/**
 * Static progress source that smooths its value through a persistent {@link ProgressTrackInfo} keyed by the box tag.
 */
class StaticBoxProgressProvider implements IBoxProgressProvider {
	private final float progress;
	private final MessageType type;
	private @Nullable ProgressTrackInfo track;

	StaticBoxProgressProvider(float progress, MessageType type) {
		this.progress = progress;
		this.type = type;
	}

	@Override
	public BoxProgress getProgress(BoxElement box, Accessor<?> accessor, float partialTicks) {
		Identifier tag = box.getTag();
		if (tag == null) {
			return new BoxProgress(progress, type);
		}
		List<ProgressView.Part> parts = List.of(ProgressView.Part.of(0, Mth.clamp(progress, 0, 1)));
		if (track == null) {
			track = JadeClient.tickHandler().progressTracker.getOrCreate(
					tag, ProgressTrackInfo.class, () -> new ProgressTrackInfo(parts, false, box.getWidth()));
		}
		track.setProgress(parts);
		track.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
		return new BoxProgress(track.getSmoothProgress(parts.getFirst()), type);
	}

}