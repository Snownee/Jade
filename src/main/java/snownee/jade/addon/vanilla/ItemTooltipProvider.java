package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class ItemTooltipProvider implements IEntityComponentProvider {
	public static final ItemTooltipProvider INSTANCE = new ItemTooltipProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void append(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ITEM_TOOLTIP)) {
			return;
		}
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		List<ITextComponent> itemTooltip = stack.getTooltip(null, TooltipFlags.NORMAL);
		if (!itemTooltip.isEmpty()) {
			itemTooltip.remove(0);
		}
		tooltip.addAll(itemTooltip);
	}
}
