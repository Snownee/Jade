package snownee.jade.addon.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.PlatformProxy;

public enum TotalEnchantmentPowerProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Level world = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		float power = 0;
		// EnchantmentMenu.java
		for (int k = -1; k <= 1; ++k) {
			for (int l = -1; l <= 1; ++l) {
				if ((k != 0 || l != 0) && world.isEmptyBlock(pos.offset(l, 0, k)) && world.isEmptyBlock(pos.offset(l, 1, k))) {
					power += getPower(world, pos.offset(l * 2, 0, k * 2));
					power += getPower(world, pos.offset(l * 2, 1, k * 2));

					if (l != 0 && k != 0) {
						power += getPower(world, pos.offset(l * 2, 0, k));
						power += getPower(world, pos.offset(l * 2, 1, k));
						power += getPower(world, pos.offset(l, 0, k * 2));
						power += getPower(world, pos.offset(l, 1, k * 2));
					}
				}
			}
		}
		if (power > 0) {
			tooltip.add(new TranslatableComponent("jade.ench_power", DisplayHelper.dfCommas.format(power)));
		}
	}

	public static float getPower(Level world, BlockPos pos) {
		return PlatformProxy.getEnchantPowerBonus(world.getBlockState(pos), world, pos);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_TOTAL_ENCHANTMENT_POWER;
	}

	@Override
	public int getDefaultPriority() {
		return -400;
	}
}
