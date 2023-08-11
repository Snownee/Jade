package snownee.jade.addon.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;

public enum ArmorStandProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ArmorStand entity = (ArmorStand) accessor.getEntity();
		boolean empty = true;
		for (ItemStack stack : entity.getArmorSlots()) {
			if (stack.isEmpty())
				continue;
			tooltip.add(IElementHelper.get().smallItem(stack));
			tooltip.append(IDisplayHelper.get().stripColor(stack.getHoverName()));
			tooltip.setLineMargin(-1, Direction2D.DOWN, -1);
			empty = false;
		}
		if (!empty) {
			tooltip.setLineMargin(-1, Direction2D.DOWN, 1);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ARMOR_STAND;
	}

}
