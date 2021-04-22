package mcp.mobius.waila.addons.minecraft;

import mcp.mobius.waila.api.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class HUDHandlerFurnace implements IComponentProvider, IServerDataProvider<TileEntity> {

	static final HUDHandlerFurnace INSTANCE = new HUDHandlerFurnace();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(PluginMinecraft.CONFIG_DISPLAY_FURNACE))
			return;

		int progressInt = accessor.getServerData().getInt("progress");
		if (progressInt == 0)
			return;

		ListNBT furnaceItems = accessor.getServerData().getList("furnace", Constants.NBT.TAG_COMPOUND);
		NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
		for (int i = 0; i < furnaceItems.size(); i++)
			inventory.set(i, ItemStack.read(furnaceItems.getCompound(i)));

		CompoundNBT progress = new CompoundNBT();
		progress.putInt("progress", progressInt);
		progress.putInt("total", accessor.getServerData().getInt("total"));

		RenderableTextComponent renderables = new RenderableTextComponent(getRenderable(inventory.get(0)), getRenderable(inventory.get(1)), new RenderableTextComponent(PluginMinecraft.RENDER_FURNACE_PROGRESS, progress), getRenderable(inventory.get(2)));

		tooltip.add(renderables);
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity blockEntity) {
		AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) blockEntity;
		ListNBT items = new ListNBT();
		items.add(furnace.getStackInSlot(0).write(new CompoundNBT()));
		items.add(furnace.getStackInSlot(1).write(new CompoundNBT()));
		items.add(furnace.getStackInSlot(2).write(new CompoundNBT()));
		data.put("furnace", items);
		CompoundNBT furnaceTag = furnace.write(new CompoundNBT());
		data.putInt("progress", furnaceTag.getInt("CookTime")); // smh
		data.putInt("total", furnaceTag.getInt("CookTimeTotal")); // smh
	}

	private static RenderableTextComponent getRenderable(ItemStack stack) {
		if (!stack.isEmpty()) {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("id", stack.getItem().getRegistryName().toString());
			tag.putInt("count", stack.getCount());
			if (stack.hasTag())
				tag.putString("nbt", stack.getTag().toString());
			return new RenderableTextComponent(PluginMinecraft.RENDER_ITEM, tag);
		} else {
			CompoundNBT spacerTag = new CompoundNBT();
			spacerTag.putInt("width", 18);
			return new RenderableTextComponent(PluginMinecraft.RENDER_SPACER, spacerTag);
		}
	}
}
