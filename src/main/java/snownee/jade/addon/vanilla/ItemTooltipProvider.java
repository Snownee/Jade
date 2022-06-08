package snownee.jade.addon.vanilla;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.jade.JadeClient;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum ItemTooltipProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		JadeClient.hideModName = true;
		List<Component> itemTooltip = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
		JadeClient.hideModName = false;
		if (!itemTooltip.isEmpty()) {
			itemTooltip.remove(0);
		}
		Font font = Minecraft.getInstance().font;
		int maxWidth = 250;
		for (Component component : itemTooltip) {
			int width = font.width(component);
			if (width > maxWidth) {
				tooltip.add(Component.literal(font.substrByWidth(component, maxWidth - 5).getString() + ".."));
			} else {
				tooltip.add(component);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ITEM_TOOLTIP;
	}

}
