package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.ProgressArrowElement;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.VanillaPlugin;

public class FurnaceProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final FurnaceProvider INSTANCE = new FurnaceProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.FURNACE))
			return;

		int progress = accessor.getServerData().getInt("progress");
		if (progress == 0)
			return;

		ListTag furnaceItems = accessor.getServerData().getList("furnace", Tag.TAG_COMPOUND);
		NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
		for (int i = 0; i < furnaceItems.size(); i++)
			inventory.set(i, ItemStack.of(furnaceItems.getCompound(i)));

		IElementHelper helper = tooltip.getElementHelper();
		int total = accessor.getServerData().getInt("total");

		tooltip.add(helper.item(inventory.get(0)));
		tooltip.append(helper.item(inventory.get(1)));
		tooltip.append(new ProgressArrowElement((float) progress / total));
		tooltip.append(helper.item(inventory.get(2)));
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity blockEntity, boolean showDetails) {
		AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) blockEntity;
		ListTag items = new ListTag();
		for (int i = 0; i < 3; i++) {
			items.add(furnace.getItem(i).serializeNBT());
		}
		data.put("furnace", items);
		CompoundTag furnaceTag = furnace.save(new CompoundTag());
		data.putInt("progress", furnaceTag.getInt("CookTime"));
		data.putInt("total", furnaceTag.getInt("CookTimeTotal"));
	}

}
