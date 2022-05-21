package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum BrewingStandProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("BrewingStand", Tag.TAG_COMPOUND)) {
			return;
		}
		CompoundTag tag = accessor.getServerData().getCompound("BrewingStand");
		int fuel = tag.getInt("Fuel");
		int time = tag.getInt("Time");
		IElementHelper helper = tooltip.getElementHelper();
		tooltip.add(Jade.smallItem(helper, new ItemStack(Items.BLAZE_POWDER)).message(null));
		tooltip.append(new TextComponent(Integer.toString(fuel)));
		if (time > 0) {
			tooltip.append(helper.spacer(5, 0));
			tooltip.append(Jade.smallItem(helper, new ItemStack(Items.CLOCK)).message(null));
			tooltip.append(new TranslatableComponent("jade.seconds", time / 20));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level arg2, BlockEntity te, boolean showDetails) {
		if (te instanceof BrewingStandBlockEntity) {
			BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) te;
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
