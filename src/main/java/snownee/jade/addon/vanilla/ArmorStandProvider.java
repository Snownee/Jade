package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.Jade;
import snownee.jade.VanillaPlugin;

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
			tooltip.add(Jade.smallItem(helper, stack));
			tooltip.append(helper.text(stack.getHoverName()));
		}
	}

}
