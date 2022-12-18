package snownee.jade.test;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TestBlockEntity extends BlockEntity {

	TestEnergyStorage energyStorage = new TestEnergyStorage();
	LazyOptional<TestEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

	FluidTank fluidStorage = new FluidTank(10000) {
		@Override
		public int getTanks() {
			return 5;
		}

		@Override
		public @NotNull FluidStack getFluidInTank(int tank) {
			if (tank > 0)
				return FluidStack.EMPTY;
			return super.getFluidInTank(tank);
		}
	};

	LazyOptional<FluidTank> fluidCap = LazyOptional.of(() -> fluidStorage);

	public TestBlockEntity(BlockPos pos, BlockState state) {
		super(Test.TILE.get(), pos, state);
	}

	@Override
	public void onChunkUnloaded() {
		energyCap.invalidate();
		fluidCap.invalidate();
		super.onChunkUnloaded();
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TestBlockEntity self) {
		self.energyStorage.energy += level.getRandom().nextInt(1000);
		if (self.energyStorage.energy > self.energyStorage.getMaxEnergyStored()) {
			self.energyStorage.energy = 0;
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == ForgeCapabilities.ENERGY) {
			return energyCap.cast();
		}
		if (cap == ForgeCapabilities.FLUID_HANDLER) {
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
			return 100000;
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
