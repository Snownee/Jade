package snownee.jade.addon.forge;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import snownee.jade.VanillaPlugin;

public class ForgeCapabilityProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final ForgeCapabilityProvider INSTANCE = new ForgeCapabilityProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		TileEntity tile = accessor.getTileEntity();
		if (tile != null) {
			if (config.get(VanillaPlugin.FORGE_ENERGY)) {
				IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY).orElse(null);
				if (storage != null && (!accessor.isServerConnected() || accessor.getServerData().contains("jadeEnergy"))) {
					IElementHelper helper = tooltip.getElementHelper();
					int cur, max;
					if (accessor.isServerConnected()) {
						cur = accessor.getServerData().getInt("jadeEnergy");
						max = accessor.getServerData().getInt("jadeMaxEnergy");
					} else {
						cur = storage.getEnergyStored();
						max = storage.getMaxEnergyStored();
					}
					String curText = TextFormatting.WHITE + VanillaPlugin.displayHelper.humanReadableNumber(cur, "FE", false) + TextFormatting.GRAY;
					String maxText = VanillaPlugin.displayHelper.humanReadableNumber(max, "FE", false);
					IFormattableTextComponent text = new TranslationTextComponent("jade.fe", curText, maxText).mergeStyle(TextFormatting.GRAY);
					IProgressStyle progressStyle = helper.progressStyle().color(0xFFFF0000, 0xFF660000);
					tooltip.add(helper.progress((float) cur / max, text, progressStyle, helper.borderStyle()).tag(VanillaPlugin.FORGE_ENERGY));
				}
			}

			if (config.get(VanillaPlugin.FORGE_FLUID)) {
				IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
				if (fluidHandler != null && (!accessor.isServerConnected() || accessor.getServerData().contains("jadeTanks"))) {
					if (accessor.isServerConnected()) {
						ListNBT list = accessor.getServerData().getList("jadeTanks", NBT.TAG_COMPOUND);
						for (INBT nbt : list) {
							CompoundNBT tankData = (CompoundNBT) nbt;
							int capacity = tankData.getInt("capacity");
							FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tankData);
							appendTank(tooltip, fluidStack, capacity);
						}
					} else {
						for (int i = 0; i < fluidHandler.getTanks(); i++) {
							appendTank(tooltip, fluidHandler.getFluidInTank(i), fluidHandler.getTankCapacity(i));
						}
					}
				}
			}
		}
	}

	public static void appendTank(ITooltip tooltip, FluidStack fluidStack, int capacity) {
		if (capacity <= 0)
			return;
		IElementHelper helper = tooltip.getElementHelper();
		ITextComponent text;
		if (fluidStack.isEmpty()) {
			text = new TranslationTextComponent("jade.fluid.empty");
		} else {
			String amountText = VanillaPlugin.displayHelper.humanReadableNumber(fluidStack.getAmount(), "B", true);
			text = new TranslationTextComponent("jade.fluid", fluidStack.getDisplayName(), amountText);
		}
		IProgressStyle progressStyle = helper.progressStyle().overlay(helper.fluid(fluidStack));
		tooltip.add(helper.progress((float) fluidStack.getAmount() / capacity, text, progressStyle, helper.borderStyle()).tag(VanillaPlugin.FORGE_FLUID));
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity tile) {
		IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY).orElse(null);
		if (storage != null) {
			data.putInt("jadeEnergy", storage.getEnergyStored());
			data.putInt("jadeMaxEnergy", storage.getMaxEnergyStored());
		}

		IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
		if (fluidHandler != null) {
			ListNBT list = new ListNBT();
			for (int i = 0; i < fluidHandler.getTanks(); i++) {
				int capacity = fluidHandler.getTankCapacity(i);
				if (capacity <= 0)
					continue;
				CompoundNBT tankData = fluidHandler.getFluidInTank(i).writeToNBT(new CompoundNBT());
				tankData.putInt("capacity", capacity);
				list.add(tankData);
			}
			if (!list.isEmpty()) {
				data.put("jadeTanks", list);
			}
		}
	}

}