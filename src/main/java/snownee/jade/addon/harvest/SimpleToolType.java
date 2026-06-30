package snownee.jade.addon.harvest;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.callback.CallbackContainer;
import snownee.jade.api.harvest.ToolResult;
import snownee.jade.api.harvest.ToolTier;
import snownee.jade.api.harvest.ToolTierAddedCallback;
import snownee.jade.api.harvest.ToolType;

public class SimpleToolType implements ToolType {

	private final Identifier uid;
	protected final List<ToolTier> tiers = Lists.newArrayList();
	protected final boolean skipInstaBreakingBlock;
	private final CallbackContainer<ToolTierAddedCallback> callbacks = new CallbackContainer<>();

	protected SimpleToolType(Identifier uid, boolean skipInstaBreakingBlock) {
		this.uid = uid;
		this.skipInstaBreakingBlock = skipInstaBreakingBlock;
	}

	public static ToolType of(Identifier uid) {
		return of(uid, true);
	}

	public static ToolType of(Identifier uid, boolean skipInstaBreakingBlock) {
		return new SimpleToolType(uid, skipInstaBreakingBlock);
	}

	@Override
	public ToolResult test(BlockState state, Level level, BlockPos pos) {
		if (skipInstaBreakingBlock && !state.requiresCorrectToolForDrops() && state.getDestroySpeed(level, pos) == 0) {
			return ToolResult.fail();
		}
		for (ToolTier tier : tiers) {
			ToolResult result = tier.isCorrectTool(state);
			if (result.isSuccess()) {
				return result;
			}
		}
		return ToolResult.fail();
	}

	@Override
	public List<ToolTier> tiers() {
		return tiers;
	}

	@Override
	public CallbackContainer<ToolTierAddedCallback> tierAddedCallbacks() {
		return callbacks;
	}

	@Override
	public Identifier getUid() {
		return uid;
	}
}
