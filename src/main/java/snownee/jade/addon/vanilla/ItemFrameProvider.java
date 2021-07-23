package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import snownee.jade.VanillaPlugin;

public class ItemFrameProvider implements IEntityComponentProvider {
	public static final ItemFrameProvider INSTANCE = new ItemFrameProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ITEM_FRAME)) {
			return;
		}
		ItemFrame itemFrame = (ItemFrame) accessor.getEntity();
		ItemStack stack = itemFrame.getItem();
		if (!stack.isEmpty()) {
			tooltip.add(stack.getHoverName());
		}
	}
}
