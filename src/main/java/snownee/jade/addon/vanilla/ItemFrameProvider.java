package snownee.jade.addon.vanilla;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ItemFrameProvider implements IEntityComponentProvider {
	public static final ItemFrameProvider INSTANCE = new ItemFrameProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
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
