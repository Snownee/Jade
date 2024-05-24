package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.CommonProxy;

public enum EnchantmentPowerProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		float power = CommonProxy.getEnchantPowerBonus(accessor.getBlockState(), accessor.getLevel(), accessor.getPosition());
		if (power > 0) {
			tooltip.add(Component.translatable("jade.ench_power", IThemeHelper.get().info(DisplayHelper.dfCommas.format(power))));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_ENCHANTMENT_POWER;
	}

	@Override
	public int getDefaultPriority() {
		return -400;
	}
}
