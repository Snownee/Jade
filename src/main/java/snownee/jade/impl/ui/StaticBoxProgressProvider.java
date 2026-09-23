package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxProgress;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IBoxProgressProvider;
import snownee.jade.api.ui.MessageType;
import snownee.jade.overlay.WailaTickHandler;
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
	public BoxProgress getProgress(IBoxElement box, @Nullable Accessor<?> accessor, float partialTicks) {
		ResourceLocation tag = box.getTag();
		if (tag == null) {
			return new BoxProgress(progress, type);
		}
		if (track == null) {
			track = WailaTickHandler.instance().progressTracker.getOrCreate(
					tag, ProgressTrackInfo.class, () -> new ProgressTrackInfo(false, progress, 0));
		}
		track.setProgress(Mth.clamp(progress, 0, 1));
		track.update(Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
		return new BoxProgress(track.getSmoothProgress(), type);
	}
}
