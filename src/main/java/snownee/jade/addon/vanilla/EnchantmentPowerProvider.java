package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class EnchantmentPowerProvider implements IComponentProvider {

	public static final EnchantmentPowerProvider INSTANCE = new EnchantmentPowerProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Level world = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		float power = 0;
		if (accessor.getBlock() instanceof EnchantmentTableBlock) {
			if (config.get(VanillaPlugin.TOTAL_ENCH_POWER)) {
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
			}
		} else if (config.get(VanillaPlugin.ENCH_POWER)) {
			power = getPower(world, pos);
		}
		if (power > 0) {
			tooltip.add(new TranslatableComponent("jade.ench_power", DisplayHelper.dfCommas.format(power)));
		}
	}

	private float getPower(Level world, BlockPos pos) {
		return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
	}
}
