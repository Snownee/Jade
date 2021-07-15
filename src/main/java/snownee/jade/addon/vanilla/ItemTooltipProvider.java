package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.JadePlugin;

public class ItemTooltipProvider implements IEntityComponentProvider {
	public static final ItemTooltipProvider INSTANCE = new ItemTooltipProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.ITEM_TOOLTIP)) {
			return;
		}
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		WailaClient.hideModName = true;
		List<ITextComponent> itemTooltip = stack.getTooltip(null, TooltipFlags.NORMAL);
		WailaClient.hideModName = false;
		if (!itemTooltip.isEmpty()) {
			itemTooltip.remove(0);
		}
		tooltip.addAll(itemTooltip);
	}
}
