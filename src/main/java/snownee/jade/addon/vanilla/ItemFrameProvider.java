package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import snownee.jade.VanillaPlugin;

public class ItemFrameProvider implements IEntityComponentProvider {
	public static final ItemFrameProvider INSTANCE = new ItemFrameProvider();

	@Override
	public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ITEM_FRAME)) {
			return;
		}
		ItemFrameEntity itemFrame = (ItemFrameEntity) accessor.getEntity();
		ItemStack stack = itemFrame.getDisplayedItem();
		if (!stack.isEmpty()) {
			tooltip.add(stack.getDisplayName());
		}
	}
}
