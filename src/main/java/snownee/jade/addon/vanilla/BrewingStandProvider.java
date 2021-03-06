package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.Jade;
import snownee.jade.VanillaPlugin;

public class BrewingStandProvider implements IComponentProvider, IServerDataProvider<TileEntity> {
	public static final BrewingStandProvider INSTANCE = new BrewingStandProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.BREWING_STAND) || !accessor.getServerData().contains("BrewingStand", Constants.NBT.TAG_COMPOUND)) {
			return;
		}
		CompoundNBT tag = accessor.getServerData().getCompound("BrewingStand");
		int fuel = tag.getInt("fuel");
		int time = tag.getInt("time");
		IElementHelper helper = tooltip.getElementHelper();
		tooltip.add(helper.item(new ItemStack(Items.BLAZE_POWDER), 0.75f));
		tooltip.append(helper.text(new TranslationTextComponent("jade.brewingStand.fuel", TextFormatting.WHITE.toString() + fuel)).translate(Jade.VERTICAL_OFFSET));
		if (time > 0) {
			tooltip.append(helper.spacer(5, 0));
			tooltip.append(helper.item(new ItemStack(Items.CLOCK), 0.75f));
			tooltip.append(helper.text(new TranslationTextComponent("jade.brewingStand.time", TextFormatting.WHITE.toString() + time / 20)).translate(Jade.VERTICAL_OFFSET));
		}
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World arg2, TileEntity te) {
		if (te instanceof BrewingStandTileEntity) {
			BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) te;
			CompoundNBT compound = new CompoundNBT();
			compound.putInt("time", brewingStand.brewTime);
			compound.putInt("fuel", brewingStand.fuel);
			tag.put("BrewingStand", compound);
		}
	}
}
