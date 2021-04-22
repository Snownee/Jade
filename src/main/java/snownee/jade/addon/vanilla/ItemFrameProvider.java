package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import snownee.jade.JadePlugin;

public class ItemFrameProvider implements IEntityComponentProvider {
	public static final ItemFrameProvider INSTANCE = new ItemFrameProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.ITEM_FRAME)) {
			return;
		}
		ItemFrameEntity itemFrame = (ItemFrameEntity) accessor.getEntity();
		ItemStack stack = itemFrame.getDisplayedItem();
		if (!stack.isEmpty()) {
			tooltip.add(stack.getDisplayName());
		}
	}
}
