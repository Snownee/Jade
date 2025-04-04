package snownee.jade.addon.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemExchangeValue;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public enum ExchangeValueProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		//ONLY FOR CRAFTMINE SNAPSHOT
		ItemStack stack = accessor.getBlock().asItem().getDefaultInstance();
		ItemExchangeValue exvComponent = stack.getComponents().get(DataComponents.EXCHANGE_VALUE);
		Player player = accessor.getPlayer();
		float originalExchangeValue = 0;
		if (exvComponent != null) {
			originalExchangeValue = exvComponent.getValue(player, stack);
		}
		double exchangeValue = (double)Math.round((double)originalExchangeValue * (double)1000.0F) / (double)1000.0F;
		if (exchangeValue != 0){
			tooltip.add(Component.translatable("item.exchange_value", Component.literal("" + exchangeValue).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.CORE_EXCHANGE_VALUE;
	}

}
