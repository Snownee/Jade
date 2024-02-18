package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;

public enum BrewingStandProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("BrewingStand", Tag.TAG_COMPOUND)) {
			return;
		}
		CompoundTag tag = accessor.getServerData().getCompound("BrewingStand");
		int fuel = tag.getInt("Fuel");
		int time = tag.getInt("Time");
		IElementHelper helper = IElementHelper.get();
		tooltip.add(helper.smallItem(new ItemStack(Items.BLAZE_POWDER)));
		tooltip.append(IThemeHelper.get().info(fuel));
		if (time > 0) {
			tooltip.append(helper.spacer(5, 0));
			tooltip.append(helper.smallItem(new ItemStack(Items.CLOCK)));
			tooltip.append(IThemeHelper.get().seconds(time));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
		if (accessor instanceof BrewingStandBlockEntity brewingStand) {
			CompoundTag compound = new CompoundTag();
			compound.putInt("Time", brewingStand.brewTime);
			compound.putInt("Fuel", brewingStand.fuel);
			tag.put("BrewingStand", compound);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_BREWING_STAND;
	}
}
