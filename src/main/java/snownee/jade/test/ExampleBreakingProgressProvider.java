package snownee.jade.test;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.block.Blocks;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBreakingProgressProvider;

public enum ExampleBreakingProgressProvider implements IBreakingProgressProvider {
	INSTANCE;

	@Override
	public @Nullable BreakingProgress getBreakingProgress(Accessor<?> accessor) {
		if (!(accessor instanceof BlockAccessor blockAccessor) ||
				blockAccessor.getBlock() != Blocks.BEDROCK ||
				!accessor.getPlayer().isShiftKeyDown()) {
			return null;
		}
		float progress = blockAccessor.getLevel().getGameTime() % 100 / 100F;
		return new BreakingProgress(progress, true);
	}
}
