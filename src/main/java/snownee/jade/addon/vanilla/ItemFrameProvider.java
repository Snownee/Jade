package snownee.jade.addon.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public enum ItemFrameProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemFrame itemFrame = (ItemFrame) accessor.getEntity();
		ItemStack stack = itemFrame.getItem();
		if (!stack.isEmpty()) {
			tooltip.add(IDisplayHelper.get().stripColor(stack.getHoverName()));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_ITEM_FRAME;
	}
}
