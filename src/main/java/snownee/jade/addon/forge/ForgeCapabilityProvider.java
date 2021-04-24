package snownee.jade.addon.forge;

import java.awt.Color;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeCapabilityProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final ForgeCapabilityProvider INSTANCE = new ForgeCapabilityProvider();

	@Override
	public void append(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		TileEntity tile = accessor.getTileEntity();
		if (tile != null) {
			IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY).orElse(null);
			if (storage != null) {
				IElementHelper helper = tooltip.getElementHelper();
				int cur = storage.getEnergyStored();
				int max = storage.getMaxEnergyStored();
				String curText = TextFormatting.WHITE + DisplayHelper.INSTANCE.humanReadableNumber(cur, "FE", false) + TextFormatting.GRAY;
				String maxText = DisplayHelper.INSTANCE.humanReadableNumber(max, "FE", false);
				ITextComponent text = new TranslationTextComponent("jade.fe", curText, maxText).mergeStyle(TextFormatting.GRAY);
				IProgressStyle progressStyle = helper.progressStyle().color(Color.RED.getRGB(), 0xFF660000);
				tooltip.add(helper.progress((float) cur / max, text, progressStyle, helper.borderStyle()));
			}
		}
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity tile) {
		IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY).orElse(null);
		if (storage != null) {
			data.putInt("jadeEnergy", storage.getEnergyStored());
			data.putInt("jadeMaxEnergy", storage.getMaxEnergyStored());
		}
	}

}
