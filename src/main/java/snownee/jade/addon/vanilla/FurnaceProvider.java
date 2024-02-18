package snownee.jade.addon.vanilla;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

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
		tooltip.append(helper.spacer(4, 0));
		tooltip.append(helper.progress((float) progress / total).translate(new Vec2(-2, 0)));
		tooltip.append(helper.item(inventory.get(2)));
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (!(accessor.getBlockEntity() instanceof AbstractFurnaceBlockEntity furnace)) {
			return;
		}
		if (furnace.isEmpty()) {
			return;
		}
		ListTag items = new ListTag();
		for (int i = 0; i < 3; i++) {
			items.add(furnace.getItem(i).save(new CompoundTag()));
		}
		data.put("furnace", items);
		CompoundTag furnaceTag = furnace.saveWithoutMetadata(accessor.getLevel().registryAccess());
		data.putInt("progress", furnaceTag.getInt("CookTime"));
		data.putInt("total", furnaceTag.getInt("CookTimeTotal"));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_FURNACE;
	}

}
