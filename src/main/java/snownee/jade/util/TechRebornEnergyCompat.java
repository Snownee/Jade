package snownee.jade.util;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import team.reborn.energy.api.EnergyStorage;

public interface TechRebornEnergyCompat {

	static BlockApiLookup<EnergyStorage, @Nullable Direction> getSided() {
		return EnergyStorage.SIDED;
	}

}
