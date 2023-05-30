package snownee.jade.addon.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
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
		for (BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
			if (EnchantmentTableBlock.isValidBookShelf(world, pos, blockpos)) {
				power += getPower(world, pos.offset(blockpos));
			}
		}

		if (power > 0) {
			tooltip.add(Component.translatable("jade.ench_power", DisplayHelper.dfCommas.format(power)));
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
