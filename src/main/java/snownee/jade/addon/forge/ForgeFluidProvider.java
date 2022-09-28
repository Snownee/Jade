package snownee.jade.addon.forge;

import com.google.common.math.IntMath;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

public enum ForgeFluidProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockEntity tile = accessor.getBlockEntity();
		if (tile == null) {
			return;
		}
		IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
		if (fluidHandler != null) {
			CompoundTag data = accessor.getServerData();
			if (!accessor.isServerConnected()) {
				appendData(data, fluidHandler);
			}
			ListTag list = data.getList("jadeTanks", Tag.TAG_COMPOUND);
			for (Tag nbt : list) {
				CompoundTag tankData = (CompoundTag) nbt;
				int capacity = tankData.getInt("capacity");
				FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tankData);
				appendTank(tooltip, fluidStack, capacity);
			}
		}
	}

	public static void appendTank(ITooltip tooltip, FluidStack fluidStack, int capacity) {
		if (capacity <= 0)
			return;
		IElementHelper helper = tooltip.getElementHelper();
		Component text;
		if (fluidStack.isEmpty()) {
			text = Component.translatable("jade.fluid.empty");
		} else {
			String amountText = VanillaPlugin.getDisplayHelper().humanReadableNumber(fluidStack.getAmount(), "B", true);
			text = Component.translatable("jade.fluid", fluidStack.getDisplayName(), amountText);
		}
		IProgressStyle progressStyle = helper.progressStyle().overlay(helper.fluid(fluidStack));
		tooltip.add(helper.progress((float) fluidStack.getAmount() / capacity, text, progressStyle, helper.borderStyle()));
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity tile, boolean showDetails) {
		IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
		if (fluidHandler != null) {
			appendData(data, fluidHandler);
		}
	}

	public static void appendData(CompoundTag data, IFluidHandler fluidHandler) {
		ListTag list = new ListTag();
		int emptyCapacity = 0;
		for (int i = 0; i < fluidHandler.getTanks(); i++) {
			int capacity = fluidHandler.getTankCapacity(i);
			if (capacity <= 0)
				continue;
			FluidStack fluidStack = fluidHandler.getFluidInTank(i);
			if (fluidStack.isEmpty()) {
				emptyCapacity = IntMath.saturatedAdd(emptyCapacity, capacity);
				continue;
			}
			CompoundTag tankData = fluidStack.writeToNBT(new CompoundTag());
			tankData.putInt("capacity", capacity);
			list.add(tankData);
		}
		if (list.isEmpty() && emptyCapacity > 0) {
			CompoundTag tankData = FluidStack.EMPTY.writeToNBT(new CompoundTag());
			tankData.putInt("capacity", emptyCapacity);
			list.add(tankData);
		}
		if (!list.isEmpty()) {
			data.put("jadeTanks", list);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.FORGE_FLUID;
	}

}
