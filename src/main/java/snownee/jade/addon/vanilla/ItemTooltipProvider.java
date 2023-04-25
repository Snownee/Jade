package snownee.jade.addon.vanilla;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import snownee.jade.JadeClient;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.TextElement;
import snownee.jade.util.ModIdentification;

public enum ItemTooltipProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		JadeClient.hideModName = true;
		List<Either<FormattedText, TooltipComponent>> lines = Lists.newArrayList();
		stack.getTooltipLines(null, TooltipFlag.Default.NORMAL).stream().map(Either::<FormattedText, TooltipComponent>left).forEach(lines::add);
		Minecraft mc = Minecraft.getInstance();
		var event = new RenderTooltipEvent.GatherComponents(stack, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), null, -1);
		MinecraftForge.EVENT_BUS.post(event);
		JadeClient.hideModName = false;
		if (event.isCanceled() || lines.isEmpty()) {
			return;
		}
		List<FormattedText> realLines = lines.stream().map($ -> $.left()).filter(Optional::isPresent).map(Optional::get).skip(1).toList();
		String modName = ModIdentification.getModName(stack);
		Font font = Minecraft.getInstance().font;
		int maxWidth = 250;
		for (FormattedText text : realLines) {
			if (ChatFormatting.stripFormatting(text.getString()).equals(modName)) {
				continue;
			}
			int width = font.width(text);
			if (width > maxWidth) {
				tooltip.add(Component.literal(font.substrByWidth(text, maxWidth - 5).getString() + ".."));
			} else {
				tooltip.add(new TextElement(text));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ITEM_TOOLTIP;
	}

}
