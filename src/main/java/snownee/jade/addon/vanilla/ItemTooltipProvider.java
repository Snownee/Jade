package snownee.jade.addon.vanilla;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TraceableException;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.WailaExceptionHandler;

public class ItemTooltipProvider implements IEntityComponentProvider {
	public static final ItemTooltipProvider INSTANCE = new ItemTooltipProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		Item.TooltipContext tooltipContext = Item.TooltipContext.of(accessor.getLevel());
		List<Component> lines = Lists.newArrayList();
		try {
			stack.getTooltipLines(tooltipContext, null, TooltipFlag.Default.NORMAL).stream().map(component -> {
				if (component.getStyle().getColor() != null) {
					return component.copy().setStyle(component.getStyle().withColor((TextColor) null));
				}
				return component;
			}).forEach(lines::add);
		} catch (Throwable e) {
			String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
			WailaExceptionHandler.handleErr(TraceableException.create(e, namespace), this, tooltip::add);
		}
		if (lines.size() < 2) {
			return;
		}
		lines.removeFirst();
		String modName = ModIdentification.getModName(stack);
		Font font = DisplayHelper.font();
		int maxWidth = 250;
		for (Component text : lines) {
			if (Objects.equals(ChatFormatting.stripFormatting(text.getString()), modName)) {
				continue;
			}
			int width = font.width(text);
			if (width > maxWidth) {
				tooltip.add(Component.literal(font.substrByWidth(text, maxWidth - 5).getString() + ".."));
			} else {
				tooltip.add(text);
			}
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ITEM_TOOLTIP;
	}

}
