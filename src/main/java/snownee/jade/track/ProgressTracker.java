package snownee.jade.track;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.resources.ResourceLocation;

public class ProgressTracker {

	private final ListMultimap<ResourceLocation, TrackInfo> map = ArrayListMultimap.create();

	public <T extends TrackInfo> T getOrCreate(ResourceLocation tag, Class<T> type, Supplier<T> supplier) {
		List<TrackInfo> infos = map.get(tag);
		T info = null;
		for (TrackInfo o : infos) {
			if (!o.updatedThisTick && type.isInstance(o)) {
				info = type.cast(o);
				break;
			}
		}
		if (info == null) {
			info = supplier.get();
			map.put(tag, info);
		}
		info.updatedThisTick = true;
		return info;
	}

	public void tick() {
		if (map.isEmpty()) {
			return;
		}
		map.values().removeIf(info -> {
			if (info.updatedThisTick && info.alive) {
				info.tick();
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
