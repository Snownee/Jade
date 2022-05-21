package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.overlay.DisplayHelper;

public enum EnchantmentPowerProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		float power = TotalEnchantmentPowerProvider.getPower(accessor.getLevel(), accessor.getPosition());
		if (power > 0) {
			tooltip.add(new TranslatableComponent("jade.ench_power", DisplayHelper.dfCommas.format(power)));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ENCHANTMENT_POWER;
	}

	@Override
	public int getDefaultPriority() {
		return -400;
	}
}
