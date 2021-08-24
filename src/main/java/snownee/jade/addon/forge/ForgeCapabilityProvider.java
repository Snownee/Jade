package snownee.jade.addon.forge;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import snownee.jade.VanillaPlugin;

public class ForgeCapabilityProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final ForgeCapabilityProvider INSTANCE = new ForgeCapabilityProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockEntity tile = accessor.getBlockEntity();
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
					String curText = ChatFormatting.WHITE + VanillaPlugin.registrar.getDisplayHelper().humanReadableNumber(cur, "FE", false) + ChatFormatting.GRAY;
					String maxText = VanillaPlugin.registrar.getDisplayHelper().humanReadableNumber(max, "FE", false);
					MutableComponent text = new TranslatableComponent("jade.fe", curText, maxText).withStyle(ChatFormatting.GRAY);
					IProgressStyle progressStyle = helper.progressStyle().color(0xFFFF0000, 0xFF660000);
					tooltip.add(helper.progress((float) cur / max, text, progressStyle, helper.borderStyle()).tag(VanillaPlugin.FORGE_ENERGY));
				}
			}

			if (config.get(VanillaPlugin.FORGE_FLUID)) {
				IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
				if (fluidHandler != null && (!accessor.isServerConnected() || accessor.getServerData().contains("jadeTanks"))) {
					if (accessor.isServerConnected()) {
						ListTag list = accessor.getServerData().getList("jadeTanks", NBT.TAG_COMPOUND);
						for (Tag nbt : list) {
							CompoundTag tankData = (CompoundTag) nbt;
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
		Component text;
		if (fluidStack.isEmpty()) {
			text = new TranslatableComponent("jade.fluid.empty");
		} else {
			String amountText = VanillaPlugin.registrar.getDisplayHelper().humanReadableNumber(fluidStack.getAmount(), "B", true);
			text = new TranslatableComponent("jade.fluid", fluidStack.getDisplayName(), amountText);
		}
		IProgressStyle progressStyle = helper.progressStyle().overlay(helper.fluid(fluidStack));
		tooltip.add(helper.progress((float) fluidStack.getAmount() / capacity, text, progressStyle, helper.borderStyle()).tag(VanillaPlugin.FORGE_FLUID));
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity tile, boolean showDetails) {
		IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY).orElse(null);
		if (storage != null) {
			data.putInt("jadeEnergy", storage.getEnergyStored());
			data.putInt("jadeMaxEnergy", storage.getMaxEnergyStored());
		}

		IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
		if (fluidHandler != null) {
			ListTag list = new ListTag();
			for (int i = 0; i < fluidHandler.getTanks(); i++) {
				int capacity = fluidHandler.getTankCapacity(i);
				if (capacity <= 0)
					continue;
				CompoundTag tankData = fluidHandler.getFluidInTank(i).writeToNBT(new CompoundTag());
				tankData.putInt("capacity", capacity);
				list.add(tankData);
			}
			if (!list.isEmpty()) {
				data.put("jadeTanks", list);
			}
		}
	}

}