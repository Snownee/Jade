package snownee.jade.addon.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.PlatformProxy;

import java.util.List;

public enum TotalEnchantmentPowerProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
				.filter(var0 -> Math.abs(var0.getX()) == 2 || Math.abs(var0.getZ()) == 2)
				.map(BlockPos::immutable)
				.toList();
		Level world = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		float power = 0;
		// EnchantmentMenu.java
		for (BlockPos blockpos : BOOKSHELF_OFFSETS) {
			// In 1.18.2, you can use
			// if (EnchantmentTableBlock.isValidBookShelf(world, pos, blockpos))
			// replace this XP
			if (world.getBlockState(pos.offset(blockpos)).is(Blocks.BOOKSHELF) && world.isEmptyBlock(pos.offset(blockpos.getX() / 2, blockpos.getY(), blockpos.getZ() / 2))) {
				power += getPower(world, pos.offset(blockpos));
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
