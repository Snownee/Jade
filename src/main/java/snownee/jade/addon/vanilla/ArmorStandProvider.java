package snownee.jade.addon.vanilla;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ScreenDirection;

public class ArmorStandProvider implements IEntityComponentProvider {
	public static final ArmorStandProvider INSTANCE = new ArmorStandProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ArmorStand entity = (ArmorStand) accessor.getEntity();
		boolean empty = true;
		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			ItemStack stack = entity.getItemBySlot(slot);
			if (stack.isEmpty()) {
				continue;
			}
			tooltip.add(JadeUI.smallItem(stack));
			tooltip.append(IDisplayHelper.get().stripColor(stack.getHoverName()));
			tooltip.setLineMargin(-1, ScreenDirection.DOWN, -1);
			empty = false;
		}
		if (!empty) {
			tooltip.setLineMargin(-1, ScreenDirection.DOWN, 1);
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ARMOR_STAND;
	}

}
