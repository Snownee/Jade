package snownee.jade.addon.vanilla;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.WailaClient;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ItemTooltipProvider implements IEntityComponentProvider {
	public static final ItemTooltipProvider INSTANCE = new ItemTooltipProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ITEM_TOOLTIP)) {
			return;
		}
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		WailaClient.hideModName = true;
		List<Component> itemTooltip = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
		WailaClient.hideModName = false;
		if (!itemTooltip.isEmpty()) {
			itemTooltip.remove(0);
		}
		Font font = Minecraft.getInstance().font;
		int maxWidth = 250;
		for (Component component : itemTooltip) {
			int width = font.width(component);
			if (width > maxWidth) {
				tooltip.add(new TextComponent(font.substrByWidth(component, maxWidth - 5).getString() + ".."));
			} else {
				tooltip.add(component);
			}
		}
	}

}
