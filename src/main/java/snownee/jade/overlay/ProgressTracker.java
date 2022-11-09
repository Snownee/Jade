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
		private int ticksSinceValueChanged;
		private boolean updatedThisTick;
		private final SmoothChasingValue progress = new SmoothChasingValue();

		public float getWidth() {
			return width;
		}

		public float tick(float pTicks) {
			progress.tick(pTicks);
			return progress.value;
		}
	}

	private final ListMultimap<ResourceLocation, TrackInfo> map = ArrayListMultimap.create();

	public TrackInfo createInfo(ResourceLocation tag, float progress, boolean canDecrease, float expectedWidth) {
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
			info.progress.start(progress);
			map.put(tag, info);
		}
		info.updatedThisTick = true;
		if (progress == info.progress.getTarget()) {
			++info.ticksSinceValueChanged;
		} else {
			if (info.ticksSinceValueChanged > 10) {
				info.progress.withSpeed(0.4F);
			} else if (canDecrease || progress >= info.progress.getTarget()) {
				float spd = Math.abs(progress - info.progress.getTarget()) / info.ticksSinceValueChanged;
				spd = Math.max(0.1F, 4F * spd);
				info.progress.withSpeed(spd);
			}
			info.ticksSinceValueChanged = 1;
		}
		if (!canDecrease && progress < info.progress.getTarget()) {
			if (info.progress.isMoving()) {
				info.progress.withSpeed(Math.max(0.5F, info.progress.getSpeed()));
				if (info.progress.getTarget() > 0.9F) {
					info.progress.target(1);
				}
			} else {
				info.progress.start(progress);
			}
		} else {
			info.progress.target(progress);
		}
		if (info.width != expectedWidth) {
			if (expectedWidth > info.width || ++info.ticksSinceWidthChanged > 10) {
				info.width = expectedWidth;
				info.ticksSinceWidthChanged = 0;
			}
		}
		return info;
	}

	public void tick() {
		map.values().removeIf(info -> {
			if (info.updatedThisTick) {
				info.updatedThisTick = false;
				return false;
			} else {
				return true;
			}
		});
	}

	public void clear() {
		map.clear();
	}

}
