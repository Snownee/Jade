package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import snownee.jade.Jade;
import snownee.jade.VanillaPlugin;

public class EnchantmentPowerProvider implements IComponentProvider {

	public static final EnchantmentPowerProvider INSTANCE = new EnchantmentPowerProvider();

	@Override
	public void append(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		World world = accessor.getWorld();
		BlockPos pos = accessor.getPosition();
		float power = 0;
		if (accessor.getBlock() instanceof EnchantingTableBlock) {
			if (config.get(VanillaPlugin.TOTAL_ENCH_POWER)) {
				// EnchantmentContainer.class
				for (int k = -1; k <= 1; ++k) {
					for (int l = -1; l <= 1; ++l) {
						if ((k != 0 || l != 0) && world.isAirBlock(pos.add(l, 0, k)) && world.isAirBlock(pos.add(l, 1, k))) {
							power += getPower(world, pos.add(l * 2, 0, k * 2));
							power += getPower(world, pos.add(l * 2, 1, k * 2));

							if (l != 0 && k != 0) {
								power += getPower(world, pos.add(l * 2, 0, k));
								power += getPower(world, pos.add(l * 2, 1, k));
								power += getPower(world, pos.add(l, 0, k * 2));
								power += getPower(world, pos.add(l, 1, k * 2));
							}
						}
					}
				}
			}
		} else if (config.get(VanillaPlugin.ENCH_POWER)) {
			power = getPower(world, pos);
		}
		if (power > 0) {
			tooltip.add(new TranslationTextComponent("jade.ench_power", TextFormatting.WHITE + Jade.dfCommas.format(power)));
		}
	}

	private float getPower(World world, BlockPos pos) {
		return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
	}
}
