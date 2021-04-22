package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.RenderableTextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.JadePlugin;
import snownee.jade.Renderables;

public class BrewingStandProvider implements IComponentProvider, IServerDataProvider<TileEntity> {
	public static final BrewingStandProvider INSTANCE = new BrewingStandProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.BREWING_STAND) || !accessor.getServerData().contains("BrewingStand", Constants.NBT.TAG_COMPOUND)) {
			return;
		}
		CompoundNBT tag = accessor.getServerData().getCompound("BrewingStand");
		int fuel = tag.getInt("fuel");
		int time = tag.getInt("time");
		RenderableTextComponent stack = Renderables.item(new ItemStack(Items.BLAZE_POWDER), 0.75f, 0);
		RenderableTextComponent fuelText = Renderables.offsetText(new TranslationTextComponent("jade.brewingStand.fuel", fuel), 0, 3);
		if (time > 0) {
			tooltip.add(Renderables.of(stack, fuelText, Renderables.spacer(5, 0), Renderables.item(new ItemStack(Items.CLOCK), 0.75f, 0), Renderables.offsetText(new TranslationTextComponent("jade.brewingStand.time", time / 20), 0, 3)));
		} else {
			tooltip.add(Renderables.of(stack, fuelText));
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
