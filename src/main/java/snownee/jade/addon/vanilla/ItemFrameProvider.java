package snownee.jade.addon.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum ItemFrameProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemFrame itemFrame = (ItemFrame) accessor.getEntity();
		ItemStack stack = itemFrame.getItem();
		if (!stack.isEmpty()) {
			tooltip.add(stack.getHoverName());
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ITEM_FRAME;
	}
}
