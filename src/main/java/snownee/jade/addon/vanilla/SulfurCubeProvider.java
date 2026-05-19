package snownee.jade.addon.vanilla;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;

public class SulfurCubeProvider implements IEntityComponentProvider {
	public static final SulfurCubeProvider INSTANCE = new SulfurCubeProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		SulfurCube sulfurCube = (SulfurCube) accessor.getEntity();
		ItemStack stack = sulfurCube.getItemBySlot(EquipmentSlot.BODY);
		if (!stack.isEmpty()) {
			tooltip.add(JadeUI.smallItem(stack));
			tooltip.append(IDisplayHelper.get().stripColor(stack.getHoverName()));
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_SULFUR_CUBE;
	}
}
