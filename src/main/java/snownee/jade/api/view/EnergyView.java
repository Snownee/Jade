package snownee.jade.api.view;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.IDisplayHelper;

@Experimental
public class EnergyView {

	public String current;
	public String max;
	public float ratio;
	@Nullable
	public Component overrideText;

	@Nullable
	public static EnergyView read(CompoundTag tag, String unit) {
		long capacity = tag.getLong("Capacity");
		if (capacity <= 0) {
			return null;
		}
		long cur = tag.getLong("Cur");
		EnergyView energyView = new EnergyView();
		energyView.current = IDisplayHelper.get().humanReadableNumber(cur, unit, false);
		energyView.max = IDisplayHelper.get().humanReadableNumber(capacity, unit, false);
		energyView.ratio = (float) cur / capacity;
		return energyView;
	}

	public static CompoundTag of(long current, long capacity) {
		CompoundTag tag = new CompoundTag();
		tag.putLong("Capacity", capacity);
		tag.putLong("Cur", current);
		return tag;
	}

}
