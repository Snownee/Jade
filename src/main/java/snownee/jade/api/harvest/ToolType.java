package snownee.jade.api.harvest;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.addon.harvest.SimpleToolType;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.callback.CallbackContainer;

public interface ToolType extends IJadeProvider {

	ToolResult test(BlockState state, Level level, BlockPos pos);

	List<ToolTier> tiers();

	CallbackContainer<ToolTierAddedCallback> tierAddedCallbacks();

	default void onTierAdded(ToolTier tier) {
		tierAddedCallbacks().call($ -> $.onAdded(this, tier.getUid(), tier));
	}

	@Nullable
	default ToolTier tier(Identifier id) {
		for (ToolTier tier : tiers()) {
			if (tier.getUid().equals(id)) {
				return tier;
			}
		}
		return null;
	}

	default ToolType addTier(ToolTier tier) {
		tiers().add(tier);
		onTierAdded(tier);
		return this;
	}

	default boolean insertTierBefore(Identifier targetTier, ToolTier tier) {
		List<ToolTier> tiers = tiers();
		for (int i = 0; i < tiers.size(); i++) {
			if (tiers.get(i).getUid().equals(targetTier)) {
				tiers.add(i, tier);
				onTierAdded(tier);
				return true;
			}
		}
		return false;
	}

	default boolean insertTierAfter(Identifier targetTier, ToolTier tier) {
		List<ToolTier> tiers = tiers();
		for (int i = 0; i < tiers.size(); i++) {
			if (tiers.get(i).getUid().equals(targetTier)) {
				tiers.add(i + 1, tier);
				onTierAdded(tier);
				return true;
			}
		}
		return false;
	}

	static ToolType of(Identifier uid) {
		return SimpleToolType.of(uid);
	}

	static ToolType of(Identifier uid, boolean skipInstaBreakingBlock) {
		return SimpleToolType.of(uid, skipInstaBreakingBlock);
	}
}
