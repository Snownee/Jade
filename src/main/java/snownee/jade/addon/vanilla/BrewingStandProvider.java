package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.Jade;
import snownee.jade.VanillaPlugin;

public class BrewingStandProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
	public static final BrewingStandProvider INSTANCE = new BrewingStandProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.BREWING_STAND) || !accessor.getServerData().contains("BrewingStand", Tag.TAG_COMPOUND)) {
			return;
		}
		CompoundTag tag = accessor.getServerData().getCompound("BrewingStand");
		int fuel = tag.getInt("fuel");
		int time = tag.getInt("time");
		IElementHelper helper = tooltip.getElementHelper();
		tooltip.add(helper.item(new ItemStack(Items.BLAZE_POWDER), 0.75f));
		tooltip.append(helper.text(new TranslatableComponent("jade.brewingStand.fuel", fuel)).translate(Jade.VERTICAL_OFFSET));
		if (time > 0) {
			tooltip.append(helper.spacer(5, 0));
			tooltip.append(helper.item(new ItemStack(Items.CLOCK), 0.75f));
			tooltip.append(helper.text(new TranslatableComponent("jade.brewingStand.time", time / 20)).translate(Jade.VERTICAL_OFFSET));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level arg2, BlockEntity te, boolean showDetails) {
		if (te instanceof BrewingStandBlockEntity) {
			BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) te;
			CompoundTag compound = new CompoundTag();
			compound.putInt("time", brewingStand.brewTime);
			compound.putInt("fuel", brewingStand.fuel);
			tag.put("BrewingStand", compound);
		}
	}
}
