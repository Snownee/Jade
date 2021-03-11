package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IElementHelper;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import snownee.jade.JadePlugin;

public class ArmorStandProvider implements IEntityComponentProvider {

    public static final ArmorStandProvider INSTANCE = new ArmorStandProvider();

    @Override
    public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!config.get(JadePlugin.ARMOR_STAND)) {
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
            tooltip.append(helper.text(stack.getDisplayName()).translate(0, 3));
        }
    }

}
