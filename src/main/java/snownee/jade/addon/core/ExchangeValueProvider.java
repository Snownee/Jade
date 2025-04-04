package snownee.jade.addon.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemExchangeValue;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum ExchangeValueProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		ItemStack stack = accessor.getPickedResult();
		ItemExchangeValue exvComponent = stack.getComponents().get(DataComponents.EXCHANGE_VALUE);
		float originalExchangeValue = 0;
		if (exvComponent != null) {
			originalExchangeValue = exvComponent.getValue(accessor.getPlayer(), stack);
		}
		double exchangeValue = (double) Math.round((double) originalExchangeValue * (double) 1000.0F) / (double) 1000.0F;
		if (exchangeValue >= config.getFloat(JadeIds.CORE_EXCHANGE_VALUE_MINIMUM)) {
			IThemeHelper helper = IThemeHelper.get();
			tooltip.add(Component.translatable("item.exchange_value", helper.info(exchangeValue)));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.CORE_EXCHANGE_VALUE;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
