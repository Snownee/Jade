package snownee.jade.addon.vanilla;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.Jade;
import snownee.jade.VanillaPlugin;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class ArmorStandProvider implements IEntityComponentProvider {

	public static final ArmorStandProvider INSTANCE = new ArmorStandProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ARMOR_STAND)) {
			return;
		}
		ArmorStand entity = (ArmorStand) accessor.getEntity();
		IElementHelper helper = tooltip.getElementHelper();
		for (ItemStack stack : entity.getArmorSlots()) {
			if (stack.isEmpty())
				continue;
			tooltip.add(Jade.smallItem(helper, stack).message(null));
			tooltip.append(helper.text(stack.getHoverName()));
		}
	}

}
