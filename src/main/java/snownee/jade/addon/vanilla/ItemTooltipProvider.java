package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

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
		String modName = ModIdentification.getModName(stack);
		itemTooltip.removeIf($ -> ChatFormatting.stripFormatting($.getString()).equals(modName));
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
