package snownee.jade.addon.vanilla;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ProgressArrowElement;

// TODO: images for light themes
public enum FurnaceProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		CompoundTag data = accessor.getServerData();
		if (!data.contains("progress")) {
			return;
		}
		int progress = data.getInt("progress");

		ListTag furnaceItems = data.getList("furnace", Tag.TAG_COMPOUND);
		NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
		for (int i = 0; i < furnaceItems.size(); i++)
			inventory.set(i, ItemStack.of(furnaceItems.getCompound(i)));

		IElementHelper helper = IElementHelper.get();
		int total = data.getInt("total");

		tooltip.add(helper.item(inventory.get(0)));
		tooltip.append(helper.item(inventory.get(1)));
		tooltip.append(new ProgressArrowElement((float) progress / total));
		tooltip.append(helper.item(inventory.get(2)));
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) accessor.getBlockEntity();
		if (furnace.isEmpty()) {
			return;
		}
		ListTag items = new ListTag();
		for (int i = 0; i < 3; i++) {
			items.add(furnace.getItem(i).save(new CompoundTag()));
		}
		data.put("furnace", items);
		CompoundTag furnaceTag = furnace.saveWithoutMetadata();
		data.putInt("progress", furnaceTag.getInt("CookTime"));
		data.putInt("total", furnaceTag.getInt("CookTimeTotal"));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_FURNACE;
	}

}
