package mcp.mobius.waila.test;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TestTileEntity extends TileEntity implements ITickableTileEntity {

	TestEnergyStorage energyStorage = new TestEnergyStorage();
	LazyOptional<TestEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

	FluidTank fluidStorage = new FluidTank(10000);
	LazyOptional<FluidTank> fluidCap = LazyOptional.of(() -> fluidStorage);

	public TestTileEntity() {
		super(Test.TILE);
	}

	@Override
	public void remove() {
		energyCap.invalidate();
		fluidCap.invalidate();
		super.remove();
	}

	@Override
	public void tick() {
		energyStorage.energy += world.rand.nextInt(10);
		if (energyStorage.energy > energyStorage.getMaxEnergyStored()) {
			energyStorage.energy = 0;
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityEnergy.ENERGY) {
			return energyCap.cast();
		}
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return fluidCap.cast();
		}
		return super.getCapability(cap, side);
	}

	public static class TestEnergyStorage implements IEnergyStorage {

		int energy;

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getEnergyStored() {
			// TODO Auto-generated method stub
			return energy;
		}

		@Override
		public int getMaxEnergyStored() {
			// TODO Auto-generated method stub
			return 10000;
		}

		@Override
		public boolean canExtract() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canReceive() {
			// TODO Auto-generated method stub
			return false;
		}

	}

}
