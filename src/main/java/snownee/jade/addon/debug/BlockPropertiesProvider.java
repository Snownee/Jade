package snownee.jade.addon.debug;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemExchangeValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum BlockPropertiesProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockBehaviour.Properties properties = accessor.getBlock().properties();
		IThemeHelper themes = IThemeHelper.get();
		tooltip.add(Component.translatable("jade.block_destroy_time", themes.info(properties.destroyTime)));
		tooltip.add(Component.translatable("jade.block_explosion_resistance", themes.info(properties.explosionResistance)));
		if (properties.jumpFactor != 1) {
			tooltip.add(Component.translatable("jade.block_jump_factor", themes.info(properties.jumpFactor)));
		}
		if (properties.speedFactor != 1) {
			tooltip.add(Component.translatable("jade.block_speed_factor", themes.info(properties.speedFactor)));
		}
		int igniteOdds = ((FireBlock) Blocks.FIRE).getIgniteOdds(accessor.getBlockState());
		if (igniteOdds != 0) {
			tooltip.add(Component.translatable("jade.block_ignite_odds", themes.info(igniteOdds)));
		}
		int burnOdds = ((FireBlock) Blocks.FIRE).getBurnOdds(accessor.getBlockState());
		if (burnOdds != 0) {
			tooltip.add(Component.translatable("jade.block_burn_odds", themes.info(burnOdds)));
		}

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
		return JadeIds.DEBUG_BLOCK_PROPERTIES;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}
}
