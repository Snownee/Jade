package snownee.jade.addon.forge;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

public enum ForgeEnergyProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockEntity tile = accessor.getBlockEntity();
		if (tile == null) {
			return;
		}
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
			String curText = ChatFormatting.WHITE + VanillaPlugin.getDisplayHelper().humanReadableNumber(cur, "FE", false) + ChatFormatting.GRAY;
			String maxText = VanillaPlugin.getDisplayHelper().humanReadableNumber(max, "FE", false);
			MutableComponent text = Component.translatable("jade.fe", curText, maxText).withStyle(ChatFormatting.GRAY);
			IProgressStyle progressStyle = helper.progressStyle().color(0xFFFF0000, 0xFF660000);
			tooltip.add(helper.progress((float) cur / max, text, progressStyle, helper.borderStyle()));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity tile, boolean showDetails) {
		IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY).orElse(null);
		if (storage != null) {
			data.putInt("jadeEnergy", storage.getEnergyStored());
			data.putInt("jadeMaxEnergy", storage.getMaxEnergyStored());
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.FORGE_ENERGY;
	}

}