package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import snownee.jade.Jade;
import snownee.jade.VanillaPlugin;

public class ArmorStandProvider implements IEntityComponentProvider {

	public static final ArmorStandProvider INSTANCE = new ArmorStandProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ARMOR_STAND)) {
			return;
		}
		ArmorStandEntity entity = (ArmorStandEntity) accessor.getEntity();
		IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
		if (itemHandler == null) {
			return;
		}
		IElementHelper helper = tooltip.getElementHelper();
		for (int i = itemHandler.getSlots() - 1; i >= 0; i--) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			tooltip.add(helper.item(stack, 0.75f));
			tooltip.append(helper.text(stack.getDisplayName()).translate(Jade.VERTICAL_OFFSET));
		}
	}

}
