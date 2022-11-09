package snownee.jade.api.view;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;
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
		int capacity = tag.getInt("Capacity");
		if (capacity <= 0) {
			return null;
		}
		int cur = tag.getInt("Cur");
		EnergyView energyView = new EnergyView();
		energyView.current = IDisplayHelper.get().humanReadableNumber(cur, unit, false);
		energyView.max = IDisplayHelper.get().humanReadableNumber(capacity, unit, false);
		energyView.ratio = (float) cur / capacity;
		return energyView;
	}

	public static CompoundTag fromForgeEnergy(IEnergyStorage storage) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Capacity", storage.getMaxEnergyStored());
		tag.putInt("Cur", storage.getEnergyStored());
		return tag;
	}

}
