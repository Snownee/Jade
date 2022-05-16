package snownee.jade.overlay;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.util.SmoothChasingValue;

public class ProgressTracker {

	public static class TrackInfo {
		private float width;
		private int ticksSinceWidthChanged;
		private boolean updatedThisTick;
		private SmoothChasingValue progress = new SmoothChasingValue();

		public float getWidth() {
			return width;
		}

		public float tick(float pTicks) {
			progress.tick(pTicks);
			return progress.value;
		}
	}

	private final ListMultimap<ResourceLocation, TrackInfo> map = ArrayListMultimap.create();

	public TrackInfo createInfo(ResourceLocation tag, float progress, float expectedWidth) {
		List<TrackInfo> infos = map.get(tag);
		TrackInfo info = null;
		for (TrackInfo o : infos) {
			if (!o.updatedThisTick) {
				info = o;
				break;
			}
		}
		if (info == null) {
			info = new TrackInfo();
			info.width = expectedWidth;
			info.progress.set(progress);
			map.put(tag, info);
		}
		info.updatedThisTick = true;
		info.progress.target(progress);
		if (info.width != expectedWidth) {
			if (expectedWidth > info.width || ++info.ticksSinceWidthChanged > 10) {
				info.width = expectedWidth;
				info.ticksSinceWidthChanged = 0;
			}
		}
		return info;
	}

	public void tick() {
		map.values().removeIf(info -> !info.updatedThisTick);
		map.values().forEach(info -> info.updatedThisTick = false);
	}

	public void clear() {
		map.clear();
	}

}
