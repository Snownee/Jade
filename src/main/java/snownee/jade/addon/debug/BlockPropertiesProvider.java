package snownee.jade.addon.debug;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
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
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.DEBUG_BLOCK_PROPERTIES;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}
}
